package me.reidj.lobby

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.reidj.lobby.ticker.Ticked

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

class TickTimerHandler(private val injects: List<Ticked>) : () -> Unit {

    private var tick = 0

    private val scope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()

    override fun invoke() {
        if (mutex.isLocked) return
        scope.launch {
            mutex.withLock {
                tick++
                injects.forEach { it.tick(tick) }
            }
        }
    }
}