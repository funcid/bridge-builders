package me.reidj.bridgebuilders.ticker.detail

import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.ticker.Ticked
import org.bukkit.Bukkit
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
            if (it.stat.realm != "" && !it.stat.isBan && System.currentTimeMillis().toInt() / 1000 >= it.stat.gameExitTime) {
                it.stat.gameLockTime = System.currentTimeMillis().toInt() / 1000 + 70
                it.stat.isBan = true
                it.stat.gameExitTime = 0
                it.stat.realm = ""
                it.player?.sendMessage(Formatting.error("Доступ к игре был заблокирован!"))
            }
            if (it.stat.isBan && System.currentTimeMillis().toInt() / 1000 >= it.stat.gameLockTime) {
                it.stat.realm = ""
                it.stat.gameLockTime = 0
                it.stat.isBan = false
                it.player?.sendMessage(Formatting.fine("Доступ к игре был разблокирован!"))
            }
        }
    }
}