package me.reidj.bridgebuilders.command

import clepto.bukkit.B
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.slots
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import ru.cristalix.core.formatting.Formatting

class AdminCommand {

    private val godSet = hashSetOf(
        "307264a1-2c69-11e8-b5ea-1cb72caa35fd", // Func
        "bf30a1df-85de-11e8-a6de-1cb72caa35fd", // Reidj
        "ca87474e-b15c-11e9-80c4-1cb72caa35fd", // Moisei
        "f5a63d7c-2da4-11ea-b816-1cb72caa35fd", // Seperinous
        "7a31c03a-5602-11ec-acca-1cb72caa35fd" // Красный_
    )

    init {
        B.regCommand(adminConsume { _, args -> slots = args[0].toInt() }, "slot", "slots")
        B.regCommand(
            adminConsume { _, args -> getByPlayer(Bukkit.getPlayer(args[0]))!!.stat.lootbox += args[1].toInt() },
            "give",
            "loot"
        )
        B.regCommand(adminConsume { user, args ->
            if (args.size == 1)
                Bukkit.getPlayer(args[0]).isOp = true
            else
                user.player!!.isOp = true
        }, "op")
        B.regCommand(
            adminConsume { _, args ->
                getByPlayer(Bukkit.getPlayer(args[0]))!!.giveMoney(args[1].toInt())
            }, "money"
        )
        B.regCommand(
            adminConsume { _, args -> getByPlayer(Bukkit.getPlayer(args[0]))!!.stat.wins += args[1].toInt() },
            "win",
            "wins"
        )
    }

    private fun adminConsume(consumer: (user: User, args: Array<String>) -> Unit): B.Executor {
        return B.Executor { currentPlayer, args ->
            if (currentPlayer.isOp || godSet.contains(currentPlayer.uniqueId.toString())) {
                consumer(getByPlayer(currentPlayer)!!, args)
                Formatting.fine("Успешно.")
            } else {
                Formatting.error("Нет прав.")
            }
        }
    }
}