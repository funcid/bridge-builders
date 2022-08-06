package me.reidj.bridgebuilders.donate

import me.reidj.bridgebuilders.data.Donate
import me.reidj.bridgebuilders.user.User
import org.bukkit.inventory.ItemStack

interface DonatePosition : Donate {

    fun getTitle(): String

    fun getDescription(): String

    fun getPrice(): Int

    fun getRare(): Rare

    fun getIcon(): ItemStack

    fun give(user: User)

    fun isActive(user: User): Boolean

    fun getName(): String

}