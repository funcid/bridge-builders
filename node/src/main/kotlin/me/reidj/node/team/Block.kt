package me.reidj.node.team

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
data class Block(val title: String, val needTotal: Int, val material: Material, val blockData: Byte = 0) {
    fun getItem(amount: Int) = ItemStack(material, amount, 0, blockData)
}
