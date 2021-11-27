
import dev.xdark.clientapi.item.Item
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.utility.*

data class Drop(
    val title: String,
    val id: Int,
    val needTotal: Int,
    val collected: Int,
    val element: RectangleElement = rectangle {
        val index = BoardBlocks.tab.children.size + 1
        val margin = 5.0
        origin = TOP_LEFT
        align = TOP_LEFT
        size = V3(200.0, 20.0)
        offset = V3(20.0 + if (index < 9) 0.0 else lastParent!!.size.x / 2, 20.0 + margin * index + size.y * index)
        color = Color(42, 102, 190, 0.2)
        addChild(item {
            origin = RIGHT
            align = RIGHT
            offset.x -= 10
            color = WHITE
            scale = V3(0.8, 0.8, 0.8)
            stack = Item.of(id).newStack(1, 0)
        })
        addChild(text {
            origin = LEFT
            align = LEFT
            offset.x += 10
            content = title
            scale = V3(0.8, 0.8)
        })
        addChild(text {
            content = "0 из $needTotal"
            origin = RIGHT
            align = RIGHT
            offset.x -= 30
            scale = V3(0.8, 0.8)
        })
    }
)
