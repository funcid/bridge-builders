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
        Bukkit.getOnlinePlayers().mapNotNull { app.getUser(it) }.forEach {
            if (it.stat.realm != "" && !it.stat.isBan && System.currentTimeMillis() / 1000 >= it.stat.leaveTime) {
                it.stat.banTime = System.currentTimeMillis().toDouble() + 1600
                it.stat.isBan = true
                it.stat.leaveTime = 0.0
                it.stat.realm = ""
                it.player?.sendMessage(Formatting.error("Доступ к игре был заблокирован!"))
            }
            if (it.stat.isBan && System.currentTimeMillis() / 1000 >= it.stat.banTime) {
                it.stat.realm = ""
                it.stat.banTime = 0.0
                it.stat.isBan = false
                it.player?.sendMessage(Formatting.fine("Доступ к игре был разблокирован!"))
            }
        }
    }
}