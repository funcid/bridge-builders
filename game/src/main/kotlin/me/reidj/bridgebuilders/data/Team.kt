package me.reidj.bridgebuilders.data

import org.bukkit.Location
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
    var breakBlocks: MutableMap<Location, Pair<Int, Byte>>,
    var bridge: Bridge,
    var collected: MutableMap<BlockPlan, Int>
) {
    fun blockPlace(block: Map.Entry<Location, Pair<Int, Byte>>) {
        block.key.block.setTypeAndDataFast(block.value.first, block.value.second)
        breakBlocks.remove(block.key)
    }
}
