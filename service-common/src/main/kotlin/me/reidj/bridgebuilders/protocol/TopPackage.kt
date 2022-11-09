package me.reidj.bridgebuilders.protocol

import me.reidj.bridgebuilders.top.PlayerTopEntry
import ru.cristalix.core.network.CorePackage

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
data class TopPackage(val topType: String, val limit: Int): CorePackage() {

    lateinit var entries: List<PlayerTopEntry<Any>>
}
