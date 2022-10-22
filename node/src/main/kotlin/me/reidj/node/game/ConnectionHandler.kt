package me.reidj.node.game

import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.conversation.ModLoader
import me.func.mod.conversation.ModTransfer
import me.func.mod.util.after
import me.func.protocol.Indicators
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import me.reidj.node.activeStatus
import me.reidj.node.app
import me.reidj.node.slots
import me.reidj.node.teams
import me.reidj.node.timer.Status
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.item.Items
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class ConnectionHandler(private val game: BridgeGame) : Listener {

    private val items = teams.map {
        Items.builder()
            .displayName("Выбрать команду: " + it.color.chatFormat + it.color.teamName)
            .type(Material.WOOL)
            .color(it.color)
            .build()
    }

    private val back = item {
        type = Material.CLAY_BALL
        nbt("other", "arrow_back")
        nbt("click", "leave")
        text("§cВернуться")
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        val uuid = player.uniqueId

        // Чтобы /spec и /rejoin работали
        if (activeStatus == Status.STARTING && Bukkit.getOnlinePlayers().size > slots) {
            after(10) { ITransferService.get().transfer(uuid, getLobbyRealm()) }
            return
        }

        val user = getUser(player)

        if (user == null) {
            player.sendMessage(Formatting.error("Нам не удалось прогрузить Вашу статистику."))
            after(10) { ITransferService.get().transfer(uuid, getLobbyRealm()) }
            return
        }

        if (user.cachedPlayer == null)
            user.cachedPlayer = player

        after(3) {
            player.setResourcePack("", "")
            player.performCommand("rp")
            ModLoader.send("mod-bundle-1.0-SNAPSHOT.jar", player)
            Anime.hideIndicator(player, Indicators.HUNGER, Indicators.EXP, Indicators.HEALTH, Indicators.VEHICLE)
        }

        player.inventory.clear()
        player.isOp = uuid.toString() in godSet

        if (activeStatus == Status.STARTING) {
            val inventory = player.inventory
            after(3) {
                player.teleport(game.getSpawnLocation())
                InteractHandler.showTeamList(player)
                val players = Bukkit.getOnlinePlayers()
                players.forEach { ModTransfer(true, slots, players.size).send("bridge:online", it) }
            }
            player.gameMode = GameMode.ADVENTURE
            inventory.setItem(8, back)
            inventory.setItem(4, ModifiersManager.modifiersItem)
            items.forEach { inventory.addItem(it) }
        } else {
            if (user.inGame) {
                user.cachedPlayer = player
                val playerTeam = teams.firstOrNull { user.team == it.color } ?: return
                playerTeam.players.add(player.uniqueId)
                playerTeam.updateNumbersPlayersInTeam()
                after(15) {
                    game.standardOperations(player, user, playerTeam)
                    user.inventory?.forEachIndexed { index, itemStack -> player.inventory.setItem(index, itemStack) }
                    player.exp = user.exp
                }
            } else {
                after(3) {
                    player.teleport(teams.random().spawn)
                    player.gameMode = GameMode.SPECTATOR
                    Bukkit.getOnlinePlayers().forEach { it.hidePlayer(app, player) }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun PlayerQuitEvent.handle() {
        val uuid = player.uniqueId
        val players = Bukkit.getOnlinePlayers()
        val team = teams.firstOrNull { uuid in it.players } ?: return
        if (activeStatus == Status.STARTING) {
            after(3) { players.forEach { ModTransfer(true, slots, players.size).send("bridge:online", it) } }
            ModifiersManager.voteRemove(player)
            userMap.remove(uuid)
            team.players.remove(uuid)
        } else if (activeStatus == Status.GAME) {
            val user = getUser(player) ?: return
            val stat = user.stat
            val inventory = player.inventory

            team.updateNumbersPlayersInTeam()

            // Удаление вещей
            DamageHandler.removeItems(
                user,
                inventory.itemInOffHand,
                player.itemOnCursor,
                *inventory.mapNotNull { it }.toTypedArray(),
                *player.openInventory.topInventory.mapNotNull { it }.toTypedArray()
            )

            user.team = team.color
            user.inventory = player.inventory
            user.exp = player.exp

            stat.lastRealm = IRealmService.get().currentRealmInfo.realmId.realmName

            team.players.remove(uuid)

            clientSocket.write(SaveUserPackage(uuid, user.stat))
        }
    }

    @EventHandler
    fun AsyncPlayerPreLoginEvent.handle() {
        if (activeStatus != Status.STARTING) {
            playerProfile.properties.forEach { profileProperty ->
                if (profileProperty.value == "PARTY_WARP" && IRealmService.get().currentRealmInfo.status != RealmStatus.WAITING_FOR_PLAYERS) {
                    disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Сейчас нельзя зайти на этот сервер")
                    loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
                }
            }
        }
    }
}