package me.reidj.bridgebuilders

import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object OnlineBar {

    val online = carved {
        enabled = true
        offset = V3(0.0, 25.0)
        origin = TOP
        align = TOP
        size = V3(180.0, 5.0, 0.0)
        color = Color(0, 0, 0, 0.62)
        +carved {
            origin = LEFT
            align = LEFT
            size = V3(0.0, 5.0, 0.0)
            color = Color(42, 102, 189, 1.0)
        }
        +text {
            origin = TOP
            align = TOP
            color = WHITE
            shadow = true
            content = "Загрузка..."
            offset.y -= 15
        }
    }

    init {
        UIEngine.overlayContext.addChild(online)
    }
}