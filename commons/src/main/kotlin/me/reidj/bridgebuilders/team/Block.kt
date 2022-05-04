package me.reidj.bridgebuilders.team

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class Block(val title: String, val needTotal: Int, val material: Material, val blockData: Byte = 0) {
    fun getItem() = ItemStack(material, 1, 0, blockData)
}
