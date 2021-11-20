
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.Context3D
import ru.cristalix.uiengine.utility.*

object TeleportCoolDown {

    val help = Context3D(V3(9.0, 96.0, -164.0))
    val text = text {
        shadow = true
        scale = V3(0.7, 0.7)
        color = WHITE
        content = "robit"
        origin = CENTER
        align = CENTER
    }
    val banner = rectangle {
        size = V3(100.0, 200.0)
        rotation = Rotation(2 * Math.PI, 0.0, 1.0, 0.0)
        color = TRANSPARENT
        addChild(text)
    }

    init {
        help + banner
        UIEngine.worldContexts + help
    }
}