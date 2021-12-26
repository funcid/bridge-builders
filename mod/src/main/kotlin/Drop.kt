import dev.xdark.clientapi.item.ItemStack
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.utility.*

data class Drop(
    val title: String,
    val icon: ItemStack,
    val needTotal: Int,
    val collected: Int,
    val element: RectangleElement = rectangle {
        val index = BoardBlocks.tab.children.size + 1
        val margin = 25.0
        origin = TOP
        align = TOP
        offset = V3(
            100 + if (index % 2 == 0) -220.0 else 20.0,
            20.0 + margin * index / 2 + size.y * index / 2 + if (index % 2 == 0) 0.0 else -margin / 2)
        size = V3(200.0, 20.0)
        color = Color(42, 102, 190, 0.2)
        addChild(item {
            origin = RIGHT
            align = RIGHT
            offset.x -= 10
            color = WHITE
            scale = V3(0.8, 0.8, 0.8)
            stack = icon
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
