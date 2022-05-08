import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*

object OnlineBar {

    val online = rectangle {
        enabled = false
        offset = V3(0.0, 25.0)
        origin = TOP
        align = TOP
        size = V3(180.0, 5.0, 0.0)
        color = Color(0, 0, 0, 0.62)
        addChild(
            rectangle {
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
                content = "Загрузка..."
                offset.y -= 15
            }
        )
    }

    init {
        UIEngine.overlayContext.addChild(online)

        mod.registerChannel("online:hide") {
            online.enabled = false
        }

        mod.registerChannel("bridge:online") {
            val max = readInt()
            val current = readInt()
            online.enabled = true
            (online.children[0] as RectangleElement).animate(1) { size.x = 180.0 / max * current }
            (online.children[1] as TextElement).content = "Ожидание игроков... [$current из $max]"
        }
    }
}