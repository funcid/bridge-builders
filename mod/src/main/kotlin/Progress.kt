import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.utility.*

data class Progress(
    var progress: Int,
    var red: Int,
    var green: Int,
    var blue: Int,
    val element: RectangleElement = rectangle {
        val index = TeamProgress.board.children.size
        val margin = 20.0
        val y = margin * index / 2 + size.y * index / 2 + if (index % 2 == 0) 0.0 else -margin / 2
        origin = TOP
        align = TOP
        offset = V3(0.0, 10.0 + y)
        size = V3(100.0, 10.0)
        color = Color(0, 0, 0, 1.0)
        addChild(rectangle {
            origin = LEFT
            align = LEFT
            size = V3(0.0, 10.0)
            color = Color(red, green, blue)
        })
        addChild(text {
            origin = LEFT
            align = LEFT
            offset = V3(-12.0, -19.5 + y)
            content = "$progress%"
            scale = V3(0.8, 0.8)
        })
    }
)
