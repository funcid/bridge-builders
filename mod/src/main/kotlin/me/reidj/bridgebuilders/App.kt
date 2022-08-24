package me.reidj.bridgebuilders

import dev.xdark.clientapi.event.render.*
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.element.RectangleElement
import ru.cristalix.uiengine.element.TextElement
import ru.cristalix.uiengine.eventloop.animate

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

lateinit var mod: App

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        mod = this

        OnlineBar
        CommandChoose
        BoardBlocks
        TeamProgress

        mod.registerChannel("bridge:online") {
            val enabled = readBoolean()
            if (enabled) {
                val max = readInt()
                val current = readInt()
                (OnlineBar.online.children[3] as RectangleElement).animate(1) { size.x = 180.0 / max * current }
                (OnlineBar.online.children[4] as TextElement).content = "Ожидание игроков... [$current из $max]"
            } else {
                OnlineBar.online.enabled = false
                CommandChoose.waiting = false
                UIEngine.overlayContext.removeChild(CommandChoose.box)

                mod.registerHandler<ArmorRender> { isCancelled = false }
                mod.registerHandler<ExpBarRender> { isCancelled = false }
                mod.registerHandler<HealthRender> { isCancelled = false }
                mod.registerHandler<HungerRender> { isCancelled = false }
                mod.registerHandler<PlayerListRender> { isCancelled = true }
            }
        }
    }
}