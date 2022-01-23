import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.ArmorRender
import dev.xdark.clientapi.event.render.ExpBarRender
import dev.xdark.clientapi.event.render.HealthRender
import dev.xdark.clientapi.event.render.HungerRender
import implario.humanize.Humanize
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.BOTTOM_RIGHT
import ru.cristalix.uiengine.utility.text

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        registerHandler<HealthRender> { isCancelled = true }
        registerHandler<ExpBarRender> { isCancelled = true }
        registerHandler<HungerRender> { isCancelled = true }
        registerHandler<ArmorRender> { isCancelled = true }

        val balanceText = text {
            content = "§aЗагрузка..."
            origin = BOTTOM_RIGHT
            align = BOTTOM_RIGHT
            shadow = true
            offset.y -= 15
            offset.x -= 3
        }

        UIEngine.overlayContext.addChild(balanceText)

        registerHandler<PluginMessage> {
            if (channel == "bridge:balance") {
                val money = data.readInt()
                balanceText.content = "§e${money} ${Humanize.plurals("монета", "монеты", "монет", money)}"
            }
        }
    }
}