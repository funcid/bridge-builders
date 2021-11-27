
import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.render.PlayerListRender
import dev.xdark.feder.NetUtil
import org.lwjgl.input.Keyboard
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.Color
import ru.cristalix.uiengine.utility.TOP
import ru.cristalix.uiengine.utility.V3
import ru.cristalix.uiengine.utility.rectangle
import kotlin.math.max

object BoardBlocks {

    val tab = rectangle {
        enabled = false
        size = V3(500.0, 350.0)
        offset.y += 50.0
        origin = TOP
        align = TOP
        color = Color(0, 0, 0, 0.86)
    }

    init {
        registerHandler<PlayerListRender> { isCancelled = true }

        UIEngine.overlayContext + tab

        App::class.mod.registerChannel("bridge:init") {
            val index = readInt()
            val needTotal = readInt()
            val collected = readInt()
            val title = NetUtil.readUtf8(this)
            val id = readInt()
            if (tab.children.size <= index)
                tab + Drop(title, id, needTotal, collected).element
            tab.enabled = true
        }

        App::class.mod.registerChannel("bridge:tabupdate") {
            val index = readInt()
            val needTotal = readInt()
            val collected = readInt()
            ((tab.children[index] as RectangleElement).children[2] as TextElement).content = "$collected из $needTotal"
            tab.children[index].animate(0.3) { color.alpha = max(1, collected) / needTotal * 0.8 + 0.2 }
        }

        registerHandler<GameLoop> {
            // Таб
            if (tab.children.isNotEmpty() && (!tab.enabled && Keyboard.isKeyDown(Keyboard.KEY_TAB))
                || (tab.enabled && !Keyboard.isKeyDown(Keyboard.KEY_TAB))) {
                tab.enabled = !tab.enabled
            }
        }
    }
}