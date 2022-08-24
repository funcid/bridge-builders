package me.reidj.bridgebuilders.top

import me.reidj.bridgebuilders.data.Stat

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class PlayerTopEntry<V>(stat: Stat, value: V) : TopEntry<Stat, V>(stat, value) {
    var userName: String? = null
    var displayName: String? = null
}