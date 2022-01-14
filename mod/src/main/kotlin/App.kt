
import dev.xdark.clientapi.event.render.*
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine

const val NAMESPACE = "bridge"
const val FILE_STORE = "http://51.38.128.132"

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        OnlineBar
        CommandChoose
        NotificationManager
        BoardBlocks

        registerHandler<HealthRender> { isCancelled = true }
        registerHandler<ExpBarRender> { isCancelled = true }
        registerHandler<HungerRender> { isCancelled = true }
        registerHandler<ArmorRender> { isCancelled = true }
        registerHandler<VehicleHealthRender> { isCancelled = true }

        registerChannel("func:glow") {
            GlowEffect.show(0.3, readInt(), readInt(), readInt(), 0.7)
        }

        registerChannel("bridge:start") {
            registerHandler<HealthRender> { isCancelled = false }
            registerHandler<HungerRender> { isCancelled = false }
        }

        /*val box = Context3D(V3(2.5, 102.0, -1.5))
        val text = text {
            content = "Сломай меня"
            shadow = true
            scale = V3(1.0, 1.0)
            color = WHITE
            origin = CENTER
            align = CENTER
        }
        val banner = rectangle {
            size = V3(100.0, 200.0)
            color = TRANSPARENT
            addChild(text)
        }

        box.addChild(banner)
        UIEngine.worldContexts.add(box)

        registerHandler<RenderTickPre> {
            val player = clientApi.minecraft().player
            val timer = clientApi.minecraft().timer
            val yaw =
                (player.rotationYaw - player.prevRotationYaw) * timer.renderPartialTicks + player.prevRotationYaw
            val pitch =
                (player.rotationPitch - player.prevRotationPitch) * timer.renderPartialTicks + player.prevRotationPitch

            box.rotation = Rotation(-yaw * Math.PI / 180 + Math.PI, 0.0, 1.0, 0.0)
            box.children[0].rotation = Rotation(-pitch * Math.PI / 180, 1.0, 0.0, 0.0)
        }*/
    }
}