package me.reidj.node.command

import me.func.mod.conversation.ModTransfer
import me.reidj.bridgebuilders.regAdminCommand
import me.reidj.node.slots
import org.bukkit.Bukkit

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class AdminCommands {

    init {
        regAdminCommand("slot") cmd@{ _, args ->
            slots = args[0].toInt()
            val players = Bukkit.getOnlinePlayers()
            players.forEach { ModTransfer(true, slots, players.size).send("bridge:online", it) }
        }
    }
}