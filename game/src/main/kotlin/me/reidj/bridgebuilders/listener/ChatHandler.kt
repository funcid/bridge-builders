package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.donate.impl.NameTag
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.teams
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService

object ChatHandler : Listener {

    private val permissionService: IPermissionService = IPermissionService.get()

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        val team = teams.filter { team -> team.players.contains(player.uniqueId) }
        if (team.isNotEmpty()) {
            isCancelled = true
            if (!message.startsWith("!")) {
                team[0].players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                    it.sendMessage("§8КОМАНДА${getPrefix(getByPlayer(player)) + message}")
                }
            } else {
                Bukkit.getOnlinePlayers().forEach {
                    it.sendMessage(
                        "" + team[0].color.chatColor + team[0].color.teamName.substring(
                            0,
                            1
                        ) + getPrefix(getByPlayer(player)) + message.drop(1)
                    )
                }
            }
        }
    }

    private fun getPrefix(user: User): String {
        var finalPrefix = ""

        permissionService.getBestGroup(user.stat.id).thenAccept { group ->
            permissionService.getNameColor(user.stat.id).thenApply {
                finalPrefix = (if (user.stat.activeNameTag == NameTag.NONE) "" else user.stat.activeNameTag.getRare()
                    .getColor() + user.stat.activeNameTag.getTitle()) + "§8 ┃ " + group.nameColor + group.prefix + "§8 ┃ §f" + (it
                    ?: group.nameColor) + user.player!!.name + " §8${Formatting.ARROW_SYMBOL + group.chatMessageColor} "
            }
        }
        return finalPrefix
    }
}