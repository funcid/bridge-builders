package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.donate.impl.NameTag
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.teams
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import ru.cristalix.core.chat.IChatService
import ru.cristalix.core.chat.IChatView
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService
import java.util.concurrent.ExecutionException

object ChatHandler : Listener {

    private val permissionService: IPermissionService = IPermissionService.get()

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        val uuid = player.uniqueId

        val chatView: IChatView = IChatService.get().getChatView(uuid)
        var error: String? = null

        // Убираю задержку в чате для персонала
        try {
            if (permissionService.getStaffGroup(player.uniqueId).get().priority >= 2500)
                chatView.resetCooldown(player.uniqueId)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        if (chatView.isSilenced && !player.hasPermission(IChatService.SILENCE_BYPASS))
            error = "Ты сейчас не можешь писать в чат!"
        else if (!player.hasPermission(IChatService.COOLDOWN_BYPASS) && chatView.isOnCooldown(uuid))
            error = "Погоди перед отправкой следующего сообщения"

        if (error != null) {
            player.sendMessage(Formatting.error(error))
            isCancelled = true
            return
        }
        val team = teams.filter { team -> team.players.contains(player.uniqueId) }
        if (team.isNotEmpty()) {
            cancel = true
            if (!message.startsWith("!")) {
                team[0].players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                    it.sendMessage(getPrefix(getByPlayer(player)) + message)
                }
            } else {
                Bukkit.getOnlinePlayers().forEach {
                    it.sendMessage(
                        "§f[" + team[0].color.chatColor + team[0].color.teamName.substring(
                            0,
                            1
                        ) + "§f] " + getPrefix(getByPlayer(player)) + message.drop(1)
                    )
                }
            }
        }
    }

    private fun getPrefix(user: User): String {
        var finalPrefix = ""

        permissionService.getBestGroup(user.stat.id).thenAccept {
            finalPrefix = (if (user.stat.activeNameTag == NameTag.NONE) "" else user.stat.activeNameTag.getRare()
                .getColor() + user.stat.activeNameTag.getTitle() +
                    "§8 ┃ " + it.nameColor + it.prefix + "§8 ┃§f ") + user.player!!.name + " §8${Formatting.ARROW_SYMBOL} §f"
        }
        return finalPrefix
    }
}