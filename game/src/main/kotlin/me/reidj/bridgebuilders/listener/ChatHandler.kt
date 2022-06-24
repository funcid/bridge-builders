package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.getPrefix
import me.reidj.bridgebuilders.isSpectator
import me.reidj.bridgebuilders.teams
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

object ChatHandler : Listener {

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        isCancelled = true
        if (isSpectator(player))
            return
        val team = teams.filter { team -> team.players.contains(player.uniqueId) }
        val user = app.getUser(player)
        if (team.isEmpty()) {
            Bukkit.getOnlinePlayers()
                .forEach { player -> user?.let { player.sendMessage(getPrefix(it, false) + message) } }
        } else if (!isSpectator(player)) {
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