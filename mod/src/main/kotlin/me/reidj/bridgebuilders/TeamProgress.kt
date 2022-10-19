package me.reidj.bridgebuilders

import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object TeamProgress {

    val board = rectangle {
        enabled = false
        size = V3(140.0, 90.0)
        origin = RIGHT
        align = RIGHT
        color = Color(0, 0, 0, 0.60)
        addChild(text {
            origin = BOTTOM
            align = BOTTOM
            color = WHITE
            scale = V3(0.8, 0.8, 0.8)
            content = "cristalix.gg"
        })
        addChild(text {
            scale = V3(0.8, 0.8, 0.8)
            offset.y = 6.0
            origin = TOP
            align = TOP
            color = WHITE
            content = "Прогресс"
        })
    }

    init {
        UIEngine.overlayContext + board

        mod.registerChannel("bridge:progressinit") {
            val index = readInt()
            val red = readInt()
            val green = readInt()
            val blue = readInt()
            if (board.children.size <= index)
                board.addChild(Progress(0, red, green, blue).element)
            board.enabled = true
        }

        mod.registerChannel("bridge:progressupdate") {
            val index = readInt()
            val needBlocks = readInt()
            val progress = readInt()
            val rectangleElement = board.children[index] as RectangleElement
            (rectangleElement.children[1] as TextElement).content = "${(progress * 1.0 / needBlocks * 100.0).toInt()}%"
            (rectangleElement.children[0] as RectangleElement).animate(1) {
                size.x = 100.0 / needBlocks * progress
            }
        }
    }
}