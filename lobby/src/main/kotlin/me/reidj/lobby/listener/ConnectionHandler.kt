package me.reidj.lobby.listener

import me.func.mod.Anime
import me.func.mod.conversation.ModLoader
import me.func.mod.util.after
import me.func.protocol.Indicators
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.godSet
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import me.reidj.bridgebuilders.userMap
import me.reidj.lobby.app
import me.reidj.lobby.content.BattlePass
import me.reidj.lobby.content.WeekRewards
import me.reidj.lobby.npc.NpcManager
import me.reidj.lobby.util.GameUtil
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
import ru.cristalix.core.transfer.ITransferService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class ConnectionHandler : Listener {

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

        player.allowFlight = IPermissionService.get().isDonator(uuid)
        player.gameMode = GameMode.ADVENTURE
        player.isOp = uuid.toString() in godSet

        after(3) {
            player.performCommand("rp")
            Anime.hideIndicator(player, Indicators.ARMOR, Indicators.EXP, Indicators.HEALTH, Indicators.HUNGER)

            ModLoader.send("lobby-mod-bundle-1.0-SNAPSHOT.jar", player)

            Anime.loadTexture(player, "https://storage.c7x.ru/reidj/bridgebuilders/ether.png")

            user.giveExperience(0)
            user.giveEther(0)

            me.func.mod.battlepass.BattlePass.send(player, BattlePass.battlePass)

            NpcManager.setNpcSkin(uuid)

            player.teleport(spawn)

            val stat = user.stat
            val now = System.currentTimeMillis()

            if (stat.lastRealm != "") {
                val realmStatus = IRealmService.get().getRealmById(RealmId.of(stat.lastRealm)).status
                if (stat.gameExitTime > 0 && stat.gameLockTime < 0 && realmStatus != RealmStatus.WAITING_FOR_PLAYERS) {
                    GameUtil.reconnect.run {
                        text = "Вернуться в игру"
                        hint = "Вернуться"
                        open(player)
                    }
                }
            }
            // Обнулить комбо сбора наград если прошло больше суток или комбо > 7
            if ((stat.rewardStreak > 0 && now - stat.lastEnter * 10000 > 24 * 60 * 60 * 1000) || stat.rewardStreak > 6) {
                stat.rewardStreak = 0
            }
            if (now - stat.dailyClaimTimestamp * 10000 > 14 * 60 * 60 * 1000) {
                Anime.close(player)
                stat.dailyClaimTimestamp = now / 10000
                Anime.openDailyRewardMenu(
                    player,
                    stat.rewardStreak,
                    *WeekRewards.values().map { it.reward }.toTypedArray()
                )

                val dailyReward = WeekRewards.values()[stat.rewardStreak]
                player.sendMessage(Formatting.fine("Ваша ежедневная награда: " + dailyReward.reward.title))
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