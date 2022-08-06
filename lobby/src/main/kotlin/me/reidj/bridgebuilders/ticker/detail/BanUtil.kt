package me.reidj.bridgebuilders.ticker.detail

import implario.humanize.Humanize
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.ticker.Ticked
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import me.reidj.bridgebuilders.packages.SaveUserPackage
import ru.cristalix.core.formatting.Formatting

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object BanUtil : Ticked {

    override fun tick(vararg args: Int) {
        if (args[0] % 20 != 0)
            return
        Bukkit.getOnlinePlayers().mapNotNull { app.getUser(it) }.forEach {
            if (it.stat.gameExitTime > 0 && !it.stat.isBan && System.currentTimeMillis().toInt() / 1000 >= it.stat.gameExitTime) {
                it.stat.gameLockTime = System.currentTimeMillis().toInt() / 1000 + 1800
                it.stat.isBan = true
                it.stat.gameExitTime = 0
                it.stat.realm = ""
                it.player?.let { player ->
                    it.player?.sendMessage(Formatting.error("Доступ к игре был заблокирован!"))
                    clientSocket.write(
                        SaveUserPackage(
                            player.uniqueId,
                            it.stat
                        )
                    )
                }
            }
            if (it.stat.isBan && System.currentTimeMillis().toInt() / 1000 >= it.stat.gameLockTime) {
                it.stat.realm = ""
                it.stat.gameLockTime = 0
                it.stat.isBan = false
                it.player?.let { player ->
                    player.sendMessage(Formatting.fine("Доступ к игре был разблокирован!"))
                    clientSocket.write(
                        SaveUserPackage(
                            player.uniqueId,
                            it.stat
                        )
                    )
                }
            }
        }
    }

    fun checkBan(user: User, player: Player): Boolean {
        if (user.stat.isBan) {
            player.sendMessage(
                Formatting.fine(
                    "До разблокировки §3${
                        convertSecond(
                            (user.stat.gameLockTime - System.currentTimeMillis().toInt() / 1000)
                        )
                    }§f."
                )
            )
            return true
        } else if (user.stat.gameExitTime > 0) {
            player.sendMessage(Formatting.error("Вы не можете начать новую игру, незакончив прошлую!"))
            return true
        }
        user.stat.gameExitTime = 0
        return false
    }

    private fun convertSecond(totalSeconds: Int): String {
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