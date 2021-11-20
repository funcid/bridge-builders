package me.reidj.bridgebuilders.data

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.scoreboard.Team
import ru.cristalix.core.formatting.Color
import java.util.*

data class Team(
    val players: MutableList<UUID>,
    val color: Color,
    var location: Location,
    var teleportLocation: Location,
    var team: Team?,
    var isActiveTeleport: Boolean,
    var breakBlocks: MutableMap<Location, Material>
)
