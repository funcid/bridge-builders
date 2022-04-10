
import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.item.ItemTools
import dev.xdark.feder.NetUtil
import org.lwjgl.input.Keyboard
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.*
import kotlin.math.max

object BoardBlocks {

    private var inGame = false

    val tab = rectangle {
        enabled = false
        size = V3(400.0, 230.0)
        offset.y += 50.0
        origin = TOP
        align = TOP
        color = Color(0, 0, 0, 0.86)
        addChild(text {
            origin = BOTTOM
            align = BOTTOM
            color = WHITE
            shadow = true
            content = "www.cristalix.ru"
        })
        addChild(rectangle {
            offset = V3(0.0, 23.0)
            origin = TOP
            align = TOP
            size = V3(380.0, 5.0, 0.0)
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
                    content = "0%"
                    offset.y -= 15
                }
            )
        })
    }

    init {
        UIEngine.overlayContext + tab

        App::class.mod.registerChannel("bridge:init") {
            val index = readInt()
            val needTotal = readInt()
            val collected = readInt()
            val title = NetUtil.readUtf8(this)
            val item = ItemTools.read(this)
            if (tab.children.size <= index)
                tab + Drop(title, item, needTotal, collected).element
            tab.enabled = true
            inGame = true
        }

        App::class.mod.registerChannel("bridge:tabupdate") {
            val index = readInt()
            val needTotal = readInt()
            val collected = readInt()
            val box = tab.children[1] as RectangleElement
            val needBlocks = readInt()
            val sum = readInt()

            ((tab.children[index] as RectangleElement).children[2] as TextElement).content = "$collected из $needTotal"
            tab.children[index].animate(0.3) { color.alpha = max(1, collected) / needTotal * 0.8 + 0.2 }

            (box.children[0] as RectangleElement).animate(1) {
                size.x = 380.0 / needBlocks * sum
            }

            (box.children[1] as TextElement).content = "${(sum * 1.0 / needBlocks * 100.0).toInt()}%"
        }

        registerHandler<GameLoop> {
            // Таб
            if (inGame && tab.children.isNotEmpty() && (!tab.enabled && Keyboard.isKeyDown(Keyboard.KEY_TAB))
                || (tab.enabled && !Keyboard.isKeyDown(Keyboard.KEY_TAB))) {
                tab.enabled = !tab.enabled
            }
            OnlineBar.online.enabled = !(!inGame && Keyboard.isKeyDown(Keyboard.KEY_TAB))
        }
    }
}