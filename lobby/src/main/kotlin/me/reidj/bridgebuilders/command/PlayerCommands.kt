package me.reidj.bridgebuilders.command

import clepto.cristalix.Cristalix
import me.func.mod.util.after
import me.func.mod.util.command
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.ticker.detail.BanUtil
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object PlayerCommands {

    init {
        command("leave") { player, _ -> Cristalix.transfer(listOf(player.uniqueId), RealmId.of(HUB)) }

        command("rejoin") { player, _ ->
            val user = app.getUser(player)!!
            if (user.stat.realm == "" || IRealmService.get()
                    .getRealmById(RealmId.of(user.stat.realm)).status == RealmStatus.WAITING_FOR_PLAYERS
            )
                return@command
            player.sendMessage(Formatting.error("У Вас нету незаконченной игры."))
            user.stat.gameExitTime = 0
            user.stat.realm = ""
            after { Cristalix.transfer(listOf(player.uniqueId), RealmId.of(user.stat.realm)) }
        }

        command("resourcepack") { player, _ -> player.setResourcePack("${STORAGE}BridgeBuilders.zip", "100") }

        command("spectate") { player, args ->
            val realmId =
                IRealmService.get().getRealmsOfType("BRD")
                    .filter { it.status == RealmStatus.GAME_STARTED_CAN_SPACTATE }
                    .map { it.realmId }
            val realm = RealmId.of("BRD-${args[0]}")
            if (realmId.contains(realm))
                Cristalix.transfer(mutableListOf(player.uniqueId), realm)
            else
                player.sendMessage(Formatting.error("Сервер не найден."))
        }

        command("game") { player, _ -> compass.open(player) }

        command("four") { player, _ ->
            val user = app.getUser(player)!!
            println(user)
            if (!BanUtil.checkBan(user, player))
                PlayerBalancer("BRI", 16).accept(player)
        }

        command("two") { player, _ ->
            val user = app.getUser(player)!!
            println(user)
            if (!BanUtil.checkBan(user, player))
                PlayerBalancer("BRD", 8).accept(player)
        }
    }
}