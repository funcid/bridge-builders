package me.reidj.bridgebuilders.map

import org.bukkit.inventory.ItemStack

data class MapData(
    val title: String,
    var requiredBlocks: MutableMap<ItemStack, Int>
)