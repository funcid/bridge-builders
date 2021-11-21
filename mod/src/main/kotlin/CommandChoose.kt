
import dev.xdark.clientapi.event.render.*
import dev.xdark.feder.NetUtil
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.*

object CommandChoose {

    private var waiting = true

    private val playerList = MutableList(4) {
        text {
            color = WHITE
            shadow = true
            offset = V3(4.0, 4.0 + it * 14.0)
        }
    }.toTypedArray()

    private val container = rectangle {
        origin = BOTTOM
        align = BOTTOM
        size = V3(104.0, 60.0)
        offset.y += size.y
        color = Color(0, 0, 0, 0.62)
        addChild(*playerList)
    }

    private val title = text {
        origin = CENTER
        align = CENTER
        color = WHITE
        shadow = true
    }

    private val box = rectangle {
        origin = BOTTOM
        align = BOTTOM
        offset.y -= 125
        size = V3(104.0, 20.0)
        addChild(title, container)
        enabled = false
    }

    init {
        UIEngine.overlayContext.addChild(box)

        playerList.forEach {
            it.content = "§7..."
        }

        val teams = mapOf(
            0 to ("§cКрасных" to Color(149, 21, 13, 0.44)),
            1 to ("§9Синих" to Color(17, 42, 168, 0.44)),
            2 to ("§aЗеленых" to Color(69, 146, 10, 0.44)),
            3 to ("§eЖелтых" to Color(202, 148, 15, 0.44))
        )

        val playerName = UIEngine.clientApi.minecraft().player.name

        App::class.mod.registerChannel("bridge:team") {
            if (!waiting)
                return@registerChannel
            val active = teams[readInt()]
            if (active == null) {
                box.enabled = false
            } else {
                box.enabled = true
                title.content = "Команда ${active.first}"
                box.color = active.second

                playerList.forEach {
                    var name = NetUtil.readUtf8(this)
                    if (name == playerName)
                        name = "§l$name"
                    it.content = name
                }
            }
        }

        registerHandler<HealthRender> { isCancelled = waiting }
        registerHandler<ExpBarRender> { isCancelled = waiting }
        registerHandler<HungerRender> { isCancelled = waiting }
        registerHandler<ArmorRender> { isCancelled = waiting }
        registerHandler<VehicleHealthRender> { isCancelled = waiting }

        App::class.mod.registerChannel("bridge:start") {
            waiting = false
            UIEngine.overlayContext.removeChild(box)
        }
    }
}