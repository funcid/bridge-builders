package me.reidj.lobby

import me.reidj.lobby.ticker.Ticked
import org.bukkit.scheduler.BukkitRunnable

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class TickTimerHandler(private vararg val ticked: Ticked): BukkitRunnable() {

    private var counter = 1

    override fun run() {
        counter++
        ticked.forEach { it.tick(counter) }
    }
}