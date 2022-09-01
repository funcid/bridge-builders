package me.reidj.lobby.command

import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.data.LootBoxType
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.protocol.SaveUserPackage
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
            val stat = user.stat
            stat.lootBoxes.add(LootBoxType.valueOf(args[1]))
            clientSocket.write(SaveUserPackage(stat.uuid, stat))
        }
        regAdminCommand("ether") { _, args ->
            val user = getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand
            val stat = user.stat
            user.giveEther(args[1].toInt())
            clientSocket.write(SaveUserPackage(stat.uuid, stat))
        }
        regAdminCommand("exp") { _, args ->
            val user = getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand
            val stat = user.stat
            user.giveExperience(args[1].toInt())
            clientSocket.write(SaveUserPackage(stat.uuid, stat))
        }
        regAdminCommand("wins") { _, args ->
            val user = getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand
            val stat = user.stat
            stat.wins += args[1].toInt()
            clientSocket.write(SaveUserPackage(stat.uuid, stat))
        }
        regAdminCommand("uban") { _, args ->
            (getUser(Bukkit.getPlayer(args[0])) ?: return@regAdminCommand).stat.run {
                gameLockTime = 0
                gameExitTime = 0
                lastRealm = ""
                clientSocket.write(SaveUserPackage(uuid, this))
            }
        }
    }
}