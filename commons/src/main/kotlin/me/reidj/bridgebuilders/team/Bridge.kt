package me.reidj.bridgebuilders.team

import org.bukkit.Location
import org.bukkit.util.Vector

data class Bridge(
    val toCenter: Vector,
    val start: Location,
    val end: Location,
    val blocks: MutableMap<Pair<Int, Byte>, MutableList<Location>> = com.google.common.collect.Maps.newConcurrentMap(),
)
