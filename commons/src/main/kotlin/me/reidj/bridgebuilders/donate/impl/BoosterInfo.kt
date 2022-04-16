package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.util.MultiTimeBar
import me.reidj.bridgebuilders.util.UtilTime
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @project BridgeBuilders
 * @author Рейдж
 */
data class BoosterInfo(val uuid: UUID, val until: Double, val time: Double, val multiplier: Double): MultiTimeBar.MultiBarInstance, DonatePosition {

    fun defaultInstance(): BoosterInfo = BoosterInfo(UUID.randomUUID(), System.currentTimeMillis() + time, time, .5)

    fun hadExpire(): Boolean = System.currentTimeMillis() > until

    override val percentsOfFullTime: Double = (until - System.currentTimeMillis()) / time * 100.0

    override val boosterTitle: String  = "§eБустер §aопыта  ${UtilTime.formatTime((until - System.currentTimeMillis()).toLong(), false)})"

    override fun getTitle(): String = "§eБустер §aопыта"

    override fun getPrice(): Int = 99

    override fun getRare(): Rare = Rare.LEGENDARY

    override fun getIcon(): ItemStack = item {
        type = org.bukkit.Material.CLAY_BALL
        nbt("other", "win1")
    }.build()

    override fun give(user: User) {
       // user.stat.donate.add(this)
    }

    override fun isActive(user: User): Boolean = true

    override fun getName(): String = "ExpBoost"

    override fun getObjectName(): String = "ExpBoost"
}
