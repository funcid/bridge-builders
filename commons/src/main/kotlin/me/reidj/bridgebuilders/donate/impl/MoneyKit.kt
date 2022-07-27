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
    val percent: Int,
    private val description: String,
) : DonatePosition {
    SMALL("Пара монет", 10, Rare.COMMON, 32, item {
        type = Material.CLAY_BALL
        nbt("other", "coin2")
    }.build(), 0,"§7Получите §e32 монеты"),
    NORMAL("Мешок монет", 39, Rare.RARE, 256, item {
        type = Material.CLAY_BALL
        nbt("other", "bag1")
    }.build(), 0, "§7Получите §e256 монет"),
    BIG("Коробка монет", 119, Rare.EPIC, 1024, item {
        type = Material.CLAY_BALL
        nbt("other", "new_lvl_rare_close")
    }.build(), 0, "§7Получите §e1024 монеты"),
    HUGE("Гора монет", 499, Rare.LEGENDARY, 8192, item {
        type = Material.TOTEM
        nbt("other", "knight")
    }.build(), 0, "§7Получите §e8192 монеты"),
    ;

    override fun getTitle(): String {
        return title
    }

    override fun getDescription(): String = description

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
        user.giveMoney(reward, true)
    }

    override fun isActive(user: User): Boolean {
        return false
    }

    override fun getName(): String {
        return name
    }

    override fun getObjectName(): String = name
}