package me.reidj.node.game

import me.reidj.bridgebuilders.createDisplayName
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.isSpectator
import me.reidj.node.activeStatus
import me.reidj.node.teams
import me.reidj.node.timer.Status
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import ru.cristalix.core.formatting.Formatting

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class ChatHandler : Listener {

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        isCancelled = true

        if (player.isSpectator())
            return

        val team = teams.firstOrNull { player.uniqueId in it.players }
        val user = getUser(player) ?: return
        val players = Bukkit.getOnlinePlayers()

        if (team == null || activeStatus != Status.GAME) {
            players.forEach { it.sendMessage("${createDisplayName(user)} ${Formatting.ARROW_SYMBOL} $message") }
        } else {
            if (!message.startsWith("!")) {
                team.players.mapNotNull { getUser(it) }.forEach {
                    it.cachedPlayer?.sendMessage("${team.commandPrefix} ${createDisplayName(user)} ${Formatting.ARROW_SYMBOL} $message")
                }
            } else {
                players.mapNotNull { getUser(it) }.forEach {
                    it.cachedPlayer?.sendMessage(
                        "${team.color.chatColor}${
                            team.color.teamName.substring(
                                0,
                                1
                            )
                        } ${createDisplayName(user)} ${Formatting.ARROW_SYMBOL} ${message.drop(1)}"
                    )
                }
            }
        }
    }
}