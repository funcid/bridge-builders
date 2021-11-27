package me.reidj.bridgebuilders.map

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class MapType(val data: MapData) {
    AQUAMARINE(
        MapData(
            "Aquamarine", mutableMapOf(
                ItemStack(Material.DIRT) to 2,
                ItemStack(Material.STONE) to 5
            )
        )
    )
}