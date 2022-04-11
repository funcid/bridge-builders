package me.reidj.bridgebuilders.util

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object DefaultKit {

    val armor = arrayOf(
        ItemStack(Material.LEATHER_BOOTS),
        ItemStack(Material.LEATHER_LEGGINGS),
        ItemStack(Material.LEATHER_CHESTPLATE),
        ItemStack(Material.LEATHER_HELMET)
    )
    val sword: ItemStack = ItemStack(Material.WOOD_SWORD)
    val pickaxe = ItemStack(Material.WOOD_PICKAXE)
    val bread = ItemStack(Material.BREAD, 32)
}