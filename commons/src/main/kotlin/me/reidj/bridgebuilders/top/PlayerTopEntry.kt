package me.reidj.bridgebuilders.top

import me.reidj.bridgebuilders.user.Stat

data class PlayerTopEntry<V>(override var key: Stat, override var value: V): TopEntry<Stat, V>(key, value) {

    lateinit var userName: String
    lateinit var displayName: String
}
