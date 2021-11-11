
import ru.cristalix.clientapi.mod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object OnlineBar {

    private val online = rectangle {
        offset = V3(0.0, 40.0)
        origin = TOP
        align = TOP
        size = V3(180.0, 5.0, 0.0)
        color = Color(0, 0, 0, 0.62)
        addChild(
            rectangle {
                origin = LEFT
                align = LEFT
                size = V3(0.0, 5.0, 0.0)
                color = Color(42, 102, 200, 1.0)
            },
            text {
                origin = TOP
                align = TOP
                color = WHITE
                shadow = true
                content = "Загрузка..."
                offset.y -= 15
            }
        )
    }

    init {
        UIEngine.overlayContext + online

        App::class.mod.registerChannel("bridge:start") {
            online.animate(2, Easings.BACK_BOTH) { offset.y = 25.0 }
        }
        App::class.mod.registerChannel("bridge:online") {
            (online.children[1] as TextElement).content = ""

            val max = readInt()
            val current = readInt()
            val waiting = readBoolean()
            if (waiting) {
                (online.children[0] as RectangleElement).animate(1) { size.x = 180.0 / max * current }
                (online.children[1] as TextElement).content = "Ожидание игроков... [$current из $max]"
            } else {
                (online.children[0] as RectangleElement).animate(1, Easings.BACK_BOTH) {
                    size.x = 180.0 - 180.0 / max * (current / 20)
                }
                val timeLess = max - current / 20
                (online.children[1] as TextElement).content =
                    "Конец игры через ${String.format("%02d:%02d", timeLess / 60, timeLess % 60)}"
            }
        }
    }
}