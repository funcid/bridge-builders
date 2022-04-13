package me.reidj.bridgebuilders.`package`

import me.reidj.bridgebuilders.top.PlayerTopEntry

data class TopPackage(val type: TopType, val limit: Int): BridgePackage() {
    lateinit var entries: List<PlayerTopEntry<Any>>

    enum class TopType {
        WINS,
        ;
    }
}
