package me.reidj.bridgebuilders.donate

import me.reidj.bridgebuilders.data.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.inventory.ItemStack

interface DonatePosition {

    fun getTitle(): String

    fun getDescription(): String

    fun getEther(): Int

    fun getCrystals(): Int

    fun getRare(): Rare

    fun getIcon(): ItemStack

    fun getLevel(): Int

    fun getTexture(): String?

    fun give(user: User)

    fun getName(): String

    fun isActive(user: User): Boolean
}