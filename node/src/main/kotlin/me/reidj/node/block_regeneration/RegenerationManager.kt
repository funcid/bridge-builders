package me.reidj.node.block_regeneration

import org.bukkit.Location
import org.bukkit.block.Block

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object RegenerationManager {

    private val blocks = mutableMapOf<Location, Pair<Int, Byte>>()

    fun placeBlock() {
        if (blocks.isEmpty())
            return
        blocks.filter { block ->
            RegenerationBlocks.values().any { block.value.first == it.id && block.value.second in it.data }
        }.forEach {
            val pair = it.value
            it.key.block.setTypeAndDataFast(pair.first, pair.second)
        }
    }

    fun addBlock(block: Block) {
        RegenerationBlocks.values().filter { block.typeId == it.id && block.data in it.data }.forEach {
            blocks[block.location] = it.id to it.data[0]
        }
        blocks.clear()
    }
}