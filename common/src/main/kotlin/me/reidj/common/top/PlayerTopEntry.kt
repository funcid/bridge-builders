package me.reidj.common.top

import me.reidj.common.user.Stat

data class PlayerTopEntry<V>(override var key: Stat, override var value: V): TopEntry<Stat, V>(key, value) {

    lateinit var userName: String
    lateinit var displayName: String
}
