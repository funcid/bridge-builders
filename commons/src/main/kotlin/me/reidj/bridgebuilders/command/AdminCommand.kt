package me.func.commons.command

import clepto.bukkit.B
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.slots
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import ru.cristalix.core.formatting.Formatting

class AdminCommand {

    private val godSet = hashSetOf(
        "307264a1-2c69-11e8-b5ea-1cb72caa35fd",
        "bf30a1df-85de-11e8-a6de-1cb72caa35fd"
    )

    init {
        B.regCommand(adminConsume { _, args -> slots = args[0].toInt() }, "slot", "slots")
        B.regCommand(
            adminConsume { _, args -> getByPlayer(Bukkit.getPlayer(args[0])).stat.lootbox += args[1].toInt() },
            "give",
            "loot"
        )
        B.regCommand(adminConsume { user, args ->
            if (args.size == 1) {
                val whoOp = Bukkit.getPlayer(args[0])
                whoOp.isOp = true
            } else {
                user.player!!.isOp = true
            }
        }, "op")
        B.regCommand(
            adminConsume { _, args -> getByPlayer(Bukkit.getPlayer(args[0])).giveMoney(args[1].toInt()) }, "money"
        )
    }

    private fun adminConsume(consumer: (user: User, args: Array<String>) -> Unit): B.Executor {
        return B.Executor { currentPlayer, args ->
            if (currentPlayer.isOp || godSet.contains(currentPlayer.uniqueId.toString())) {
                consumer(getByPlayer(currentPlayer), args)
                Formatting.fine("Успешно.")
            } else {
                Formatting.error("Нет прав.")
            }
        }
    }
}