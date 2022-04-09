package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.donate.impl.NameTag
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import ru.cristalix.core.CoreApi
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.karma.IKarmaService
import ru.cristalix.core.karma.KarmaService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.permissions.IPermissionService
import java.util.function.Predicate

object ChatHandler : Listener {

    private val permissionService: IPermissionService = IPermissionService.get()

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        if (activeStatus != Status.GAME)
            return
        IKarmaService.get().enableGG(Predicate.isEqual(player.uniqueId))
        val team = teams.filter { team -> team.players.contains(player.uniqueId) }
        if (team.isNotEmpty() || app.isSpectator(player)) {
            isCancelled = true
            if (!message.startsWith("!")) {
                team[0].players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                    it.sendMessage("§8КОМАНДА ${getPrefix(getByPlayer(player)) + message}")
                }
            } else {
                Bukkit.getOnlinePlayers().forEach {
                    it.sendMessage(
                        "" + team[0].color.chatColor + team[0].color.teamName.substring(
                            0,
                            1
                        ) + " " + getPrefix(getByPlayer(player)) + message.drop(1)
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
                    .getColor() + user.stat.activeNameTag.getTitle() + "§8 ┃ ") + (if (group.prefix == "") "" else group.nameColor + group.prefix + "§8 ┃ §f") + (it
                    ?: group.nameColor) + user.player!!.name + " §8${Formatting.ARROW_SYMBOL + group.chatMessageColor} "

                    /*finalPrefix = (if (user.stat.activeNameTag == NameTag.NONE) "" else user.stat.activeNameTag.getRare()
                    .getColor() + user.stat.activeNameTag.getTitle()) + (if (group.prefix == "") "" else "§8 ┃ ") + group.nameColor + group.prefix + "§8 ┃ §f" + (it
                    ?: group.nameColor) + user.player!!.name + " §8${Formatting.ARROW_SYMBOL + group.chatMessageColor} "*/
            }
        }
        return finalPrefix
    }
}