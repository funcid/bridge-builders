package me.reidj.node.team

import me.reidj.bridgebuilders.worldMeta
import me.reidj.node.map.MapType
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.util.Vector

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
data class Bridge(
    val toCenter: Vector,
    val start: Location,
    val end: Location,
    val blocks: MutableMap<Pair<Int, Byte>, MutableList<Location>> = com.google.common.collect.Maps.newConcurrentMap(),
) {

    fun placeBlock(team: Team) {
        if (team.blocksToPlace <= 0)
            return
        val toPlace = team.collected.filter {
            (blocks[it.key.material.id to it.key.blockData]
                ?: listOf()).size > it.key.needTotal - it.value
        }
        var nearest: Location? = null
        var data: Pair<Int, Byte>? = null
        blocks.filter { (key, _) ->
            toPlace.keys.any { it.material.id == key.first }
        }.forEach { (key, value) ->
            if (nearest == null) {
                value.forEach { nearest = it }
                data = key
            }
        }
        if (nearest != null) {
            nearest?.block?.setTypeIdAndData(data!!.first, data!!.second, false)
            team.blocksToPlace--
            blocks[data]?.let {
                if (it.isEmpty())
                    blocks.remove(data)
                else
                    it.remove(nearest)
            }
        } else {
            team.blocksToPlace = 0
        }
    }

    fun generateBridge(map: MapType) = Bridge(toCenter, start, end, blocks).apply {
        getBridge(map).forEach { current ->
            val currentBlock = current.block.type.id to current.block.data
            val blockList = blocks[currentBlock]
            if (blockList != null)
                blockList.add(current)
            else
                blocks[currentBlock] = mutableListOf(current)
            current.block.setTypeAndDataFast(0, 0)
        }
    }

    private fun getBridge(map: MapType) = mutableListOf<Location>().apply {
        val vector = toCenter
        val bridge = Bridge(vector, start, end, blocks)
        val width = 16
        repeat(map.length) { len ->
            repeat(width) { xOrZ ->
                repeat(map.height) { y ->
                    add(
                        Location(
                            worldMeta.world,
                            bridge.start.x + len * vector.x + xOrZ * vector.z,
                            bridge.start.y + y,
                            bridge.start.z + len * vector.z + xOrZ * vector.x,
                        )
                    )
                }
            }
        }
    }

    fun blockOfBridge(block: Block, map: MapType) = block.location in getBridge(map)
}
