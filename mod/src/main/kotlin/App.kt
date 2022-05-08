
import dev.xdark.clientapi.event.render.*
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine

lateinit var mod: App

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        mod = this

        TeamProgress
        OnlineBar
        CommandChoose
        BoardBlocks

        registerHandler<HealthRender> { isCancelled = true }
        registerHandler<ExpBarRender> { isCancelled = true }
        registerHandler<HungerRender> { isCancelled = true }
        registerHandler<ArmorRender> { isCancelled = true }
        registerHandler<VehicleHealthRender> { isCancelled = true }

        registerChannel("bridge:start") {
            registerHandler<ArmorRender> { isCancelled = false }
            registerHandler<ExpBarRender> { isCancelled = false }
            registerHandler<HealthRender> { isCancelled = false }
            registerHandler<HungerRender> { isCancelled = false }
            registerHandler<PlayerListRender> { isCancelled = true }
        }
    }
}