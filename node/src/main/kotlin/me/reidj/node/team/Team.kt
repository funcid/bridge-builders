package me.reidj.node.team

import me.func.mod.Anime
import me.reidj.node.map.MapType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Color
import java.util.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
data class Team(
    val players: MutableSet<UUID>,
    val color: Color,
    val spawn: Location,
    var teleport: Location,
    val spawnYaw: Float,
    val spawnPitch: Float,
    var bridge: Bridge,
    var collected: MutableMap<Block, Int>,
    var breakBlocks: MutableMap<Location, Pair<Int, Byte>>,
    var blocksToPlace: Int = 0
) {
    val commandPrefix = "§8КОМАНДА"

    fun baseTeleport(player: Player) = player.teleport(spawn.apply {
        yaw = spawnYaw
        pitch = spawnPitch
    })

    fun getCountBlocksTeam(map: MapType) = collected.map { it.value }.sum() < map.needBlocks

    fun updateNumbersPlayersInTeam() = players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
        Anime.bottomRightMessage(it, "Игроков в команде §8>> §a${players.size}")
    }
}
