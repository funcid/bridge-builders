package me.reidj.bridgebuilders

import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

lateinit var mod: KotlinMod

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        mod = this

        LevelIndicator()
        RightBottom()
    }
}