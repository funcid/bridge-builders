
import ru.cristalix.clientapi.mod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object TeamProgress {

    val board = rectangle {
        size = V3(140.0, 140.0)
        offset = V3(420.0, 40.0)
        origin = CENTER
        align = CENTER
        color = Color(0, 0, 0, 0.60)
        addChild(text {
            origin = BOTTOM
            align = BOTTOM
            color = WHITE
            scale = V3(0.8, 0.8, 0.8)
            content = "www.cristalix.ru"
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

        App::class.mod.registerChannel("bridge:progressupdate") {
            val index = readInt()
            val needBlocks = readInt()
            val progress = readInt()
            val rectangleElement = board.children[index] as RectangleElement

            (rectangleElement.children[1] as TextElement).content = "${(progress * 1.0 / needBlocks * 100.0).toInt()}%"
            (rectangleElement.children[0] as RectangleElement).animate(1) {
                size.x = 100.0 / needBlocks * progress
            }
        }

        App::class.mod.registerChannel("bridge:progressinit") {
            val index = readInt()
            val red = readInt()
            val green = readInt()
            val blue = readInt()
            if (board.children.size <= index)
                board + Progress(0, red, green, blue).element
        }
    }
}