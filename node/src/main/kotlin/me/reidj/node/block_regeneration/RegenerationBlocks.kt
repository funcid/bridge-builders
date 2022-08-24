package me.reidj.node.block_regeneration

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class RegenerationBlocks(val id: Int, vararg val data: Byte) {
    IRON_ORE(15),
    DIAMOND_ORE(56),
    COAL_ORE(16),
    GOLD_ORE(14),
    LOG(17, 0, 1, 2),
    PLANKS(5, 0, 1, 2),
    ANDESITE(1, 5),
    SAND(12),
    STAINED_HARDENED_CLAY(159, 10),
    TNT(46),
    ;
}