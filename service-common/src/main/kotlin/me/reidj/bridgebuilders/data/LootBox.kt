package me.reidj.bridgebuilders.data

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
data class LootBox(
    val rare: Rare,
    val ether: Int,
    val crystals: Int,
    val openPrice: Int,
    val openLevel: Int,
)
