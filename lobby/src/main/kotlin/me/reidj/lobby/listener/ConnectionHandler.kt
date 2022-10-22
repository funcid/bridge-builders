package me.reidj.lobby.listener

import me.func.mod.Anime
import me.func.mod.conversation.ModLoader
import me.func.mod.ui.menu.button
import me.func.mod.ui.menu.dailyReward
import me.func.mod.ui.menu.recconnct.Reconnect
import me.func.mod.ui.scoreboard.ScoreBoard
import me.func.mod.util.after
import me.func.protocol.ui.indicator.Indicators
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.godSet
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import me.reidj.bridgebuilders.userMap
import me.reidj.lobby.app
import me.reidj.lobby.content.WeekRewards
import me.reidj.lobby.util.GameUtil.spawn
import me.reidj.lobby.util.ItemUtil.backItem
import me.reidj.lobby.util.ItemUtil.cosmeticItem
import me.reidj.lobby.util.ItemUtil.gameItem
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.tab.ITabService
import ru.cristalix.core.transfer.ITransferService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class ConnectionHandler : Listener {

    init {
        ScoreBoard.builder()
            .key("scoreboard")
            .header("BridgeBuilders")
            .dynamic("Уровень") { "§b${getUser(it)?.getLevel()}" }
            .dynamic("Эфира") { "§d${getUser(it)?.stat?.ether}" }
            .empty()
            .dynamic("Онлайн") { IRealmService.get().getOnlineOnRealms("BRIL") }
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        val user = getUser(player)
        val uuid = player.uniqueId

        if (user == null) {
            player.sendMessage(Formatting.error("Нам не удалось прогрузить Вашу статистику."))
            after(10) { ITransferService.get().transfer(uuid, app.getHub()) }
            return
        }

        if (user.cachedPlayer == null)
            user.cachedPlayer = player

        player.isOp = uuid.toString() in godSet
        player.allowFlight = IPermissionService.get().isDonator(uuid)
        player.gameMode = GameMode.ADVENTURE

        ITabService.get().update(player)

        after(3) {
            player.setResourcePack("", "")

            if (user.stat.isApprovedResourcepack)
                player.performCommand("rp")

            ScoreBoard.subscribe("scoreboard", player)

            Anime.hideIndicator(player, Indicators.ARMOR, Indicators.EXP, Indicators.HEALTH, Indicators.HUNGER)

            ModLoader.send("lobby-mod-bundle-1.0-SNAPSHOT.jar", player)

            user.giveExperience(0)
            user.giveEther(0)

            player.teleport(spawn)

            val stat = user.stat
            val now = System.currentTimeMillis()

            if (stat.lastRealm != "") {
                val realmStatus = IRealmService.get().getRealmById(RealmId.of(stat.lastRealm)).status
                if (realmStatus != RealmStatus.WAITING_FOR_PLAYERS) {
                    Reconnect {
                        val click = getUser(it) ?: return@Reconnect
                        if (user.isArmLock)
                            return@Reconnect
                        click.isArmLock = true
                        it.performCommand("rejoin")
                        after(5) { click.isArmLock = false }
                    }.open(player)
                }
            }
            // Обнулить комбо сбора наград если прошло больше суток или комбо > 7
            if ((stat.rewardStreak > 0 && now - stat.lastEnter * 10000 > 24 * 60 * 60 * 1000) || stat.rewardStreak > 6) {
                stat.rewardStreak = 0
            }
            if (now - stat.dailyClaimTimestamp * 10000 > 14 * 60 * 60 * 1000) {
                Anime.close(player)
                stat.dailyClaimTimestamp = now / 10000
                dailyReward {
                    currentDay = stat.rewardStreak
                    storage = WeekRewards.values().map {
                        button {
                            title = it.title
                            item = it.icon
                        }
                    }.toMutableList()
                }.open(player)
                val dailyReward = WeekRewards.values()[stat.rewardStreak]
                player.sendMessage(Formatting.fine("Ваша ежедневная награда: " + dailyReward.title))
                dailyReward.give(user)
                stat.rewardStreak++
            }
            stat.lastEnter = now / 10000
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        val uuid = player.uniqueId
        val user = userMap.remove(uuid) ?: return
        clientSocket.write(SaveUserPackage(uuid, user.stat))
    }

    @EventHandler
    fun PlayerSpawnLocationEvent.handle() {
        player.inventory.setItem(0, gameItem)
        player.inventory.setItem(4, cosmeticItem)
        player.inventory.setItem(8, backItem)
    }
}