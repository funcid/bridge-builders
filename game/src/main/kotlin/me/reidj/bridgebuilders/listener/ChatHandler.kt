package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.*
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

object ChatHandler : Listener {

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        if (activeStatus != Status.GAME || isSpectator(player))
            return
        val team = teams.filter { team -> team.players.contains(player.uniqueId) }
        if (team.isNotEmpty() && !isSpectator(player)) {
            isCancelled = true
            val user = app.getUser(player)
            if (!message.startsWith("!")) {
                team[0].players.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
                    user?.let { player.sendMessage("§8КОМАНДА ${getPrefix(it, false) + message}") }
                }
            } else {
                Bukkit.getOnlinePlayers().forEach { player ->
                    user?.let {
                        player.sendMessage(
                            "" + team[0].color.chatColor + team[0].color.teamName.substring(
                                0,
                                1
                            ) + " " + getPrefix(it, false) + message.drop(1)
                        )
                    }

                }
            }

        }
    }
}