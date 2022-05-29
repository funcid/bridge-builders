package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class MoneyKit(
    private val title: String,
    private val price: Int,
    private val rare: Rare,
    private val reward: Int,
    private val item: ItemStack,
    val percent: Int = 0
) : DonatePosition {
    SMALL("Пара монет", 10, Rare.COMMON, 32, item {
        type = Material.CLAY_BALL
        text("§eПара монет §7> §f32\n\n§7Получите §e32 монеты\n§7за §b10 кристаликов§7.")
        nbt("other", "coin2")
    }.build()),
    NORMAL("Мешок монет", 39, Rare.RARE, 256, item {
        type = Material.CLAY_BALL
        text("§eПара монет §7> §f128\n\n§7Получите §e256 монет\n§7за §b39 кристаликов§7.")
        nbt("other", "bag1")
    }.build()),
    BIG("Коробка монет", 119, Rare.EPIC, 1024, item {
        type = Material.CLAY_BALL
        text("§eПара монет §7> §f1024\n\n§7Получите §e1024 монеты\n§7за §b119 кристаликов§7.")
        nbt("other", "new_lvl_rare_close")
    }.build()),
    HUGE("Гора монет", 499, Rare.LEGENDARY, 8192, item {
        type = Material.TOTEM
        text("§eПара монет §7> §f8192\n\n§7Получите §e8192 монеты\n§7за §b499 кристаликов§7.")
        nbt("other", "knight")
    }.build()), ;

    override fun getTitle(): String {
        return title
    }

    override fun getPrice(): Int {
        return price
    }

    override fun getRare(): Rare {
        return rare
    }

    override fun getIcon(): ItemStack {
        return item
    }

    override fun give(user: User) {
        user.giveMoney(reward)
    }

    override fun isActive(user: User): Boolean {
        return false
    }

    override fun getName(): String {
        return name
    }

    override fun getObjectName(): String = name
}