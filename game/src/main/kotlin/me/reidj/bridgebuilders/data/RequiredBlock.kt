package me.reidj.bridgebuilders.data

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class RequiredBlock(val title: String, var collected: Int, val needTotal: Int, val item: Material, val id: Int) {

    fun getItem(icon: Material, id: Int, ): ItemStack {
        val item =
            org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack.asNMSCopy(
                ItemStack(icon)
            )
        item.data = id
        return item.asBukkitMirror()
    }
}
