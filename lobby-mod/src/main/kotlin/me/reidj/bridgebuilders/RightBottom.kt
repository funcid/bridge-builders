package me.reidj.bridgebuilders

import dev.xdark.clientapi.resource.ResourceLocation
import dev.xdark.feder.NetUtil
import implario.humanize.Humanize
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.utility.*

class RightBottom {
    private val box = rectangle {
        origin = BOTTOM_RIGHT
        align = BOTTOM_RIGHT
        color = TRANSPARENT
        size = V3(50.0, 70.0)
        +text {
            origin = BOTTOM_RIGHT
            align = BOTTOM_RIGHT
            shadow = true
            offset.y -= 15
            offset.x -= 3
        }
    }
    private val icon = rectangle {
        origin = BOTTOM_RIGHT
        align = BOTTOM_RIGHT
        color = WHITE
        size = V3(13.0, 13.0, 13.0)
        offset.x -= 57
        offset.y -= 13
    }

    init {
        UIEngine.overlayContext + box

        mod.registerChannel("bridge:bottom") {
            val content = readInt()
            val image = NetUtil.readUtf8(this)
            val new = Humanize.plurals("Эфир", "Эфира", "Эфира", content)

            val text = (box.children[0] as TextElement)
            text.content = "§d$new $content"

            if (image != null && icon.textureLocation == null) {
                icon.textureLocation = ResourceLocation.of("cache/animation", image)
                box + icon
            }
        }
    }
}