package me.reidj.bridgebuilders.data

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.scoreboard.Team
import ru.cristalix.core.formatting.Color
import java.util.*

data class Team(
    val players: MutableList<UUID>,
    val color: Color,
    var spawn: Location,
    var teleport: Location,
    var team: Team?,
    var isActiveTeleport: Boolean,
    var breakBlocks: MutableMap<Location, Material>,
    var requiredBlocks: MutableMap<Int, RequiredBlock>,
    var collectedBlocks: Int,
    var bridge: MutableList<Location>,
    var blocksConstruction: MutableList<Block>
)
