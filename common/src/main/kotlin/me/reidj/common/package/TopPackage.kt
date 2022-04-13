package me.reidj.common.`package`

import me.reidj.common.top.PlayerTopEntry


data class TopPackage(val type: TopType, val limit: Int): BridgePackage() {
    lateinit var entries: List<PlayerTopEntry<Any>>

    enum class TopType {
        WINS,
        ;
    }
}
