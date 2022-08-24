package me.reidj.lobby.command

import me.func.mod.Anime
import me.func.mod.battlepass.BattlePass
import me.func.mod.util.after
import me.func.mod.util.command
import me.func.protocol.battlepass.BattlePassUserData
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.protocol.RejoinPackage
import me.reidj.lobby.PlayerBalancer
import me.reidj.lobby.app
import me.reidj.lobby.ticker.detail.BanManager
import me.reidj.lobby.util.GameUtil
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class PlayerCommands {

    init {
        command("rp") { player, _ -> player.setResourcePack(System.getenv("resourcepack"), "3845agc8-219j-12ed-861d-0242ac120002") }
        command("leave") { player, _ -> ITransferService.get().transfer(player.uniqueId, app.getHub()) }
        command("spectate") { player, args ->
            val realmId = IRealmService.get().getRealmsOfType("BRIT")
                .filter { it.status == RealmStatus.GAME_STARTED_CAN_SPACTATE }
                .map { it.realmId }
            val realm = RealmId.of("BRIT-${args[0]}")
            if (realm in realmId)
                ITransferService.get().transfer(player.uniqueId, realm)
            else
                player.sendMessage(Formatting.error("Сервер не найден."))
        }
        command("rejoin") { player, _ ->
            val user = getUser(player) ?: return@command
            val stat = user.stat
            val realm = IRealmService.get().getRealmById(RealmId.of(stat.lastRealm))

            if (stat.lastRealm == "" || realm.status == RealmStatus.WAITING_FOR_PLAYERS) {
                val message = "У вас нету незаконченной игры!"
                player.sendMessage(Formatting.fine(message))
                Anime.killboardMessage(player, message)
                return@command
            }
            stat.gameExitTime = -1
            stat.lastRealm = ""

            clientSocket.write(RejoinPackage(stat.uuid))

            after(3) { ITransferService.get().transfer(player.uniqueId, realm.realmId) }
        }
        command("game") { player, _ -> GameUtil.compass.open(player) }
        command("two") { player, _ ->
            val user = getUser(player) ?: return@command
            if (!BanManager.endOfBan(user))
                PlayerBalancer("BRD", 8).accept(player)
        }
        command("test") { player, _ ->
            BattlePass.show(player, me.reidj.lobby.content.BattlePass.battlePass, BattlePassUserData(100, false))
        }
    }
}