package me.reidj.bridgebuilders.util

import ru.cristalix.core.CoreApi
import ru.cristalix.core.display.enums.EnumPosition
import ru.cristalix.core.formatting.Color
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

/**
 * @project BridgeBuilders
 * @author Рейдж
 */
class MultiTimeBar constructor(
    itemList: Supplier<List<MultiBarInstance>>?,
    reloadTime: Long,
    reloadTimeUnit: TimeUnit?,
    ifEmpty: Supplier<String>?
) {

    private var supplier: Supplier<List<MultiBarInstance>>? = null
    private var ifEmpty: Supplier<String>? = null
    private var bar: CriTimeBar? = null
    private val currentInt = AtomicInteger(0)
    private var current: MultiBarInstance? = null

    init {
        supplier = itemList
        this.ifEmpty = ifEmpty
        refreshCurrent()
        bar = CriTimeBar(EnumPosition.TOPTOP, "null", 0.5f, Color.GREEN)
        refreshTimeBar()
        CoreApi.get().platform.scheduler.runSyncRepeating({ refreshTimeBar() }, 1L, TimeUnit.SECONDS)
        CoreApi.get().platform.scheduler.runSyncRepeating({
            refreshCurrent()
            refreshTimeBar()
        }, reloadTime, reloadTimeUnit)
    }

    fun onJoin(user: UUID) {
        bar!!.add(user)
    }

    fun onQuit(user: UUID) {
        bar!!.remove(user)
    }

    private fun refreshTimeBar() {
        if (current == null) {
            bar!!.title = ifEmpty!!.get()
            bar!!.percent = 1f
            bar!!.update()
            return
        }
        var chance = (current!!.percentsOfFullTime / 100).toFloat()
        if (chance > 1) chance = 1f
        if (chance < 0) chance = 0f
        bar!!.percent = chance
        bar!!.title = current!!.boosterTitle
        bar!!.color = Color.values()[currentInt.get()]
        bar!!.update()
    }

    private fun refreshCurrent() {
        val list = supplier!!.get()
        if (list == null || list.isEmpty()) {
            current = null
            return
        }
        var j = currentInt.getAndIncrement()
        if (j >= list.size) {
            j = 0
            currentInt.set(1)
        }
        current = list[j]
    }

    interface MultiBarInstance {
        val percentsOfFullTime: Double
        val boosterTitle: String?
    }
}