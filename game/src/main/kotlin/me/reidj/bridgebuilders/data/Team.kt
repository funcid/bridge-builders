package me.reidj.bridgebuilders.data

import org.bukkit.Location
import org.bukkit.Material
import ru.cristalix.core.formatting.Color
import java.util.*

data class Team(
    val players: MutableList<UUID>,
    val color: Color,
    var spawn: Location,
    var teleport: Location,
    var yaw: Float,
    var pitch: Float,
    var isActiveTeleport: Boolean,
    var breakBlocks: MutableMap<Location, Material>,
    var bridge: Bridge,
    var collected: MutableMap<BlockPlan, Int>
)
