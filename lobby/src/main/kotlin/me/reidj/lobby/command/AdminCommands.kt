package me.reidj.lobby.command

import me.reidj.bridgebuilders.data.LootBoxType
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.regAdminCommand
import org.bukkit.Bukkit

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class AdminCommands {

    init {
        regAdminCommand("loot") { _, args ->
            val user = getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand
            user.stat.lootBoxes.add(LootBoxType.valueOf(args[1]))
        }
        regAdminCommand("ether") { _, args ->
            val user = getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand
            user.giveEther(args[1].toInt())
        }
        regAdminCommand("exp") { _, args ->
            val user = getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand
            user.giveExperience(args[1].toInt())
        }
        regAdminCommand("wins") { _, args ->
            val user = getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand
            user.stat.wins += args[1].toInt()
        }
        regAdminCommand("uban") { _, args ->
            (getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand).stat.run {
                gameLockTime = 0
                lastRealm = ""
            }
        }
    }
}