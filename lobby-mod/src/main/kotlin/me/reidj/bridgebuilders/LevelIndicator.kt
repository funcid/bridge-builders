package me.reidj.bridgebuilders

import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class LevelIndicator {

    private val levelBar = carved {
        enabled = true
        offset = V3(0.0, -27.0)
        origin = BOTTOM
        align = BOTTOM
        size = V3(180.0, 5.0, 0.0)
        color = Color(0, 0, 0, 0.62)
        addChild(
            carved {
                origin = LEFT
                align = LEFT
                size = V3(0.0, 5.0, 0.0)
                color = Color(42, 102, 189, 1.0)
            },
            text {
                origin = TOP
                align = TOP
                color = WHITE
                shadow = true
                content = "Загрузка... "
                offset.y -= 15
            }
        )
    }

    init {
        UIEngine.overlayContext.addChild(levelBar)

        mod.registerChannel("bridge:exp") {
            val level = readInt()
            val experience = readDouble()
            val requiredExperience = readInt()

            (levelBar.children[3] as RectangleElement).animate(1) {
                size.x = 180.0 / requiredExperience * experience
            }
            (levelBar.children[4] as TextElement).content = "Уровень §b${level} §7${experience.toInt()} из $requiredExperience"
        }
    }
}