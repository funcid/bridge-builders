package me.reidj.bridgebuilders.util

import ru.cristalix.core.display.IDisplayService
import ru.cristalix.core.display.enums.EnumPosition
import ru.cristalix.core.display.enums.EnumUpdateType
import ru.cristalix.core.display.messages.ProgressMessage
import ru.cristalix.core.formatting.Color
import java.util.*

/**
 * @project BridgeBuilders
 * @author Рейдж
 */
class CriTimeBar constructor(position: EnumPosition?, title: String?, percent: Float, color: Color?) {
    private val loop: MutableList<UUID> = ArrayList()

    var enumPosition: EnumPosition? = null
    var percent = 0f
    var title: String? = null
    var color: Color? = null

    init {
        enumPosition = position
        this.percent = percent
        this.title = title
        this.color = color
    }

    fun add(uid: UUID) {
        loop.add(uid)
        send(uid, build(EnumUpdateType.ADD))
    }

    fun remove(uid: UUID) {
        if (loop.remove(uid)) send(uid, build(EnumUpdateType.REMOVE))
    }

    fun update() {
        sendLoop(build(EnumUpdateType.UPDATE))
    }

    private fun sendLoop(message: ProgressMessage) {
        IDisplayService.get().sendProgress(loop, message)
    }

    private fun send(uid: UUID, message: ProgressMessage) {
        IDisplayService.get().sendProgress(uid, message)
    }

    private fun build(type: EnumUpdateType): ProgressMessage {
        return ProgressMessage.builder().updateType(type).position(enumPosition).color(color).percent(percent)
            .name(title).build()
    }
}