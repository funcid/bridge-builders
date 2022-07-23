package me.reidj.bridgebuilders.command

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import implario.humanize.Humanize
import me.reidj.bridgebuilders.HUB
import me.reidj.bridgebuilders.PlayerBalancer
import me.reidj.bridgebuilders.STORAGE
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.util.MenuUtil
import org.bukkit.entity.Player
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
        // Команда выхода в хаб
        B.regCommand({ player, _ ->
            Cristalix.transfer(listOf(player.uniqueId), RealmId.of(HUB))
            null
        }, "leave")

        // Вернуться в игру
        B.regCommand({ player: Player, _ ->
            val user = app.getUser(player)!!
            if (user.stat.realm == "" || IRealmService.get()
                    .getRealmById(RealmId.of(user.stat.realm)).status == RealmStatus.WAITING_FOR_PLAYERS
            )
                return@regCommand Formatting.error("У Вас нету незаконченной игры.")
            Cristalix.transfer(listOf(player.uniqueId), RealmId.of(user.stat.realm))
            null
        }, "rejoin")

        // Загруза ресурспака
        B.regCommand({ player: Player, _ ->
            player.setResourcePack("${STORAGE}BridgeBuilders.zip", "100")
            null
        }, "resourcepack")

        // Спекать за игрой
        B.regCommand({ player, args ->
            val realmId =
                IRealmService.get().getRealmsOfType("BRD")
                    .filter { it.status == RealmStatus.GAME_STARTED_CAN_SPACTATE }
                    .map { it.realmId }
            val realm = RealmId.of("BRD-${args[0]}")
            if (realmId.contains(realm))
                Cristalix.transfer(mutableListOf(player.uniqueId), realm)
            else
                player.sendMessage(Formatting.error("Сервер не найден."))
            null
        }, "spectate", "spec")

        B.regCommand({ player, _ ->
            MenuUtil.compass.open(player)
            null
        }, "game")

        B.regCommand({ player, _ ->
            if (!checkBan(app.getUser(player)!!))
                PlayerBalancer("BRI", 16).accept(player)
            null
        }, "four")

        B.regCommand({ player, _ ->
            if (!checkBan(app.getUser(player)!!))
                PlayerBalancer("BRD", 8).accept(player)
            null
        }, "two")
    }

    private fun checkBan(user: User): Boolean {
        if (user.stat.isBan) {
            user.player?.sendMessage(
                Formatting.fine(
                    "До разблокировки §3${
                        convertSecond(
                            user.stat.banTime.toInt() - System.currentTimeMillis().toInt() / 1000
                        )
                    }§f."
                )
            )
            return true
        }
        return false
    }

    private fun convertSecond(totalSeconds: Int): String {
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "$minutes ${
            Humanize.plurals(
                "минута",
                "минуты",
                "минут",
                minutes
            )
        } $seconds ${Humanize.plurals("секунда", "секунды", "секунд", seconds)}"
    }
}