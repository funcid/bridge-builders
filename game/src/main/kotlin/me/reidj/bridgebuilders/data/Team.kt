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
    fun blockReturn() {
        breakBlocks.forEach { block ->
            block.key.block.setTypeAndDataFast(
                block.value.first,
                block.value.second
            )
        }
    }
}

