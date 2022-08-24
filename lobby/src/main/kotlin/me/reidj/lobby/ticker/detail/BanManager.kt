package me.reidj.lobby.ticker.detail

import implario.humanize.Humanize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.func.mod.Anime
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import me.reidj.bridgebuilders.user.User
import me.reidj.lobby.ticker.Ticked
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Formatting

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class BanManager : Ticked {

    override fun tick(args: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            if (args % 20 != 0)
                return@launch
            Bukkit.getOnlinePlayers().mapNotNull { getUser(it) }.forEach {
                val stat = it.stat
                val now = System.currentTimeMillis() / 1000
                if (stat.gameExitTime in 1..now) {
                    stat.gameLockTime = now + 1800
                    stat.gameExitTime = -1
                    stat.lastRealm = ""
                    clientSocket.write(SaveUserPackage(stat.uuid, stat))
                    sendMessage(it.cachedPlayer ?: return@launch, "Доступ к игре был заблокирован!", false)
                } else if (stat.gameLockTime in 1..now) {
                    stat.gameLockTime = -1
                    clientSocket.write(SaveUserPackage(stat.uuid, stat))
                    sendMessage(it.cachedPlayer ?: return@launch, "Доступ к игре был разблокирован!", true)
                }
            }
        }
    }

    private fun sendMessage(player: Player, message: String, isFine: Boolean) {
        player.sendMessage(Formatting.fine(message))
        Anime.bigTitle(player, (if (isFine) "§a" else "§c") + message)
    }

    companion object {
        fun endOfBan(user: User): Boolean {
            val stat = user.stat
            if (stat.gameLockTime > 0) {
                user.cachedPlayer?.sendMessage(Formatting.fine(
                    "До разблокировки §3${
                        timeConverter(
                            (stat.gameLockTime - (System.currentTimeMillis() / 1000)).toInt()
                        )
                    }§f."
                ))
                return true
            } else if (stat.gameExitTime > 0) {
                user.cachedPlayer?.sendMessage(Formatting.error("Вы не можете начать новую игру, незакончив прошлую!"))
                return true
            }
            return false
        }

        private fun timeConverter(totalSeconds: Int): String {
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return "$minutes ${
                Humanize.plurals(
                    "минута",
                    "минуты",
                    "минут",
                    minutes
                )
            } $seconds ${Humanize.plurals("секунда", "секунды", "секунд", seconds)}"
        }
    }
}