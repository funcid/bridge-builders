package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

enum class StarterPack(
    private val title: String,
    private val price: Int,
    private val description: String,
) : DonatePosition {
    STARTER_PACK("§bСтартовый набор", 89, "§7Вы получите §b3 лутбокса\n" +
            "§7и §e512 монет§7."),
    ;
    override fun getTitle(): String = title

    override fun getPrice(): Int = price

    override fun getRare(): Rare = Rare.LEGENDARY

    override fun getIcon(): ItemStack = item {
        type = Material.CLAY_BALL
        enchant(Enchantment.LUCK, 0)
        nbt("other", "unique")
        nbt("HideFlags", 63)
    }.build()

    override fun getDescription(): String = description

    override fun give(user: User) {
        user.stat.lootbox += 3
        user.giveMoney(512)
    }

    override fun isActive(user: User): Boolean = false

    override fun getName(): String = "StarterPack"

    override fun getObjectName(): String = "StarterPack"


}