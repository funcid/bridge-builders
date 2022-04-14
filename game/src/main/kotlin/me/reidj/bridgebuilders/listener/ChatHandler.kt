package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.Status
import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.donate.impl.NameTag
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
        if (activeStatus != Status.GAME)
            return
        val team = teams.filter { team -> team.players.contains(player.uniqueId) }
        if (team.isNotEmpty() || app.isSpectator(player)) {
            isCancelled = true
            app.getUser(player)?.let { user ->
                if (!message.startsWith("!")) {
                    team[0].players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                        it.sendMessage("§8КОМАНДА ${getPrefix(user) + message}")
                    }
                } else {
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage(
                            "" + team[0].color.chatColor + team[0].color.teamName.substring(
                                0,
                                1
                            ) + " " + getPrefix(user) + message.drop(1)
                        )
                    }
                }
            }

        }
    }

    private fun getPrefix(user: User): String {
        var finalPrefix = ""

        permissionService.getBestGroup(user.stat.uuid).thenAccept { group ->
            permissionService.getNameColor(user.stat.uuid).thenApply {
                finalPrefix = (if (user.stat.activeNameTag == data.NameTag.NONE) "" else NameTag.valueOf(user.stat.activeNameTag.name).getRare()
                    .getColor() + NameTag.valueOf(user.stat.activeNameTag.name).getTitle() + "§8 ┃ ") + (if (group.prefix == "") "" else group.nameColor + group.prefix + "§8 ┃ §f") + (it
                    ?: group.nameColor) + user.player!!.name + " §8${Formatting.ARROW_SYMBOL + group.chatMessageColor} "
            }
        }
        return finalPrefix
    }
}