package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.conversation.ModLoader
import me.func.mod.conversation.ModTransfer
import me.func.mod.util.after
import me.func.protocol.Marker
import me.func.protocol.MarkerSign
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.packages.SaveUserPackage
import me.reidj.bridgebuilders.util.DefaultKit
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.item.Items
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import java.util.*

object ConnectionHandler : Listener {

    private val back = item {
        type = Material.CLAY_BALL
        nbt("other", "cancel")
        text("§cВернуться")
    }.build()

    val markers = mutableListOf<Marker>()

    @EventHandler
    fun PlayerJoinEvent.handle() {
        val uuid = player.uniqueId
        // Чтобы /spec и /rejoin работали
        if (activeStatus == Status.STARTING && Bukkit.getOnlinePlayers().size > slots) {
            Cristalix.transfer(listOf(uuid), LOBBY_SERVER)
            return
        }

        val user = app.getUser(uuid)

        if (user == null) {
            player.sendMessage(Formatting.error("Нам не удалось прогрузить Вашу статистику."))
            after(10) { Cristalix.transfer(setOf(player.uniqueId), LOBBY_SERVER) }
            return
        }

        if (user.stat.isBan || user.stat.gameExitTime > 0 || user.stat.gameLockTime > 0) {
            player.sendMessage(Formatting.error("На Вас наложена временная блокировка или Вы не закончили прошлую игру!"))
            after(10) { Cristalix.transfer(setOf(player.uniqueId), LOBBY_SERVER) }
            return
        }

        player.inventory.clear()

        after(15) {
            user.player = player
            ModLoader.send("mod-bundle-1.0-SNAPSHOT.jar", player)
            // Создание маркера
            teams.forEach {
                markers.add(
                    Anime.marker(
                        player,
                        Marker(
                            UUID.randomUUID(),
                            it.teleport.x + 0.5,
                            it.teleport.y + 1.5,
                            it.teleport.z + 0.5,
                            16.0,
                            MarkerSign.ARROW_DOWN.texture
                        )
                    )
                )
            }
            // Движение маркера
            markers.forEach { marker ->
                var up = false
                B.repeat(15) {
                    up = !up
                    Anime.moveMarker(
                        player,
                        marker.uuid,
                        marker.x,
                        marker.y - if (up) 0.0 else 0.7,
                        marker.z,
                        0.75
                    )
                }
            }
        }

        if (activeStatus == Status.STARTING) {
            after(15) {
                player.teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5))
                player.gameMode = GameMode.ADVENTURE
                player.inventory.setItem(8, back)
                teams.forEach {
                    player.inventory.addItem(
                        Items.builder()
                            .displayName("Выбрать команду: " + it.color.chatFormat + it.color.teamName)
                            .type(Material.WOOL)
                            .color(it.color)
                            .build()
                    )
                }
            }
        } else {
            if (user.inGame) {
                teams.filter { it.spawn == user.team!!.spawn }[0].players.add(uuid)
                B.postpone(15) {
                    DefaultKit.init(player)
                    ModTransfer().send("bridge:start", player)
                    app.updateNumbersPlayersInTeam()
                    Anime.timer(player, "Конец игры через", activeStatus.lastSecond - timer.time % 20)
                    Anime.sendEmptyBuffer("online:hide", player)
                    user.inventory!!.forEachIndexed { index, itemStack -> player.inventory.setItem(index, itemStack) }
                    player.exp = user.exp
                }
            } else {
                after(15) { player.teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5)) }
                player.gameMode = GameMode.SPECTATOR
                Bukkit.getOnlinePlayers().forEach { it.hidePlayer(app, player) }
            }
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        val uuid = player.uniqueId
        val allTeams = teams.filter { uuid in it.players }
        if (allTeams.isEmpty())
            return
        val team = allTeams[0]
        team.players.remove(uuid)
        if (activeStatus == Status.GAME && !isSpectator(player)) {
            val user = app.getUser(player) ?: return

            if (!user.inGame)
                return

            player.inventory.filterNotNull().forEach { DamageListener.removeItems(user, it) }
            DamageListener.removeItems(user, player.itemOnCursor)
            player.openInventory.topInventory.filterNotNull().forEach { DamageListener.removeItems(user, it) }

            user.stat.realm = IRealmService.get().currentRealmInfo.realmId.realmName

            user.stat.gameExitTime = (System.currentTimeMillis() / 1000 + 300).toInt()

            user.team = team
            user.inventory = player.inventory
            user.exp = player.exp

            app.updateNumbersPlayersInTeam()

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