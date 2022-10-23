package me.reidj.node.util

import dev.implario.bukkit.platform.Platforms
import dev.implario.bukkit.routine.Routine
import me.reidj.bridgebuilders.getLobbyRealm
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.transfer.ITransferService
import java.util.function.Consumer

private val routines = hashSetOf<Routine>()

inline fun everyAfter(after: Long, every: Long, noinline r: (Routine) -> Unit) = after(after) { every(every, r) }

fun after(ticks: Long, action: Consumer<Routine>): Routine {
    val routine = Routine()
    routine.interval = ticks
    routine.doLast(action)
    val taskId = intArrayOf(Bukkit.getScheduler().scheduleSyncDelayedTask(Platforms.getPlugin(), {
        routines.remove(routine)
        routine.action.accept(routine)
    }, ticks))
    routines.add(routine)
    routine.id = taskId[0]
    return routine
}

fun Player.kickPlayer() {
    sendMessage(Formatting.error("Нам не удалось прогрузить Вашу статистику."))
    me.func.mod.util.after(10) { ITransferService.get().transfer(uniqueId, getLobbyRealm()) }
}

fun every(ticks: Long, action: Consumer<Routine>): Routine {
    val routine = Routine()
    routine.interval = ticks
    routine.doLast(action)
    val taskId = intArrayOf(
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            Platforms.getPlugin(),
            {
                routine.nextPassTime = System.currentTimeMillis() + ticks * 50L
                routine.action.accept(routine)
                routine.pass = routine.pass + 1L
                if (routine.passLimit != 0L && routine.pass >= routine.passLimit) {
                    routine.killHandler.accept(routine)
                }
            }, ticks, ticks
        )
    )
    routines.add(routine)
    routine.onKill {
        routines.remove(routine)
        Bukkit.getScheduler().cancelTask(taskId[0])
    }
    return routine
}
