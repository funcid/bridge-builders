package me.reidj.lobby

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.reidj.lobby.ticker.Ticked
import org.bukkit.scheduler.BukkitRunnable

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

private const val RESET_PERIOD = 20 * 60L * 10

class TickTimerHandler(private val injects: List<Ticked>) : () -> Unit, BukkitRunnable() {

    private var tick = 0

    private val scope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()

    override fun invoke() {
        if (mutex.isLocked) return
        scope.launch {
            mutex.withLock {
                if (tick % RESET_PERIOD == 0L) {
                    tick = 1
                } else {
                    tick++
                }
                injects.forEach { it.tick(tick) }
            }
        }
    }

    override fun run() {
        invoke()
    }
}