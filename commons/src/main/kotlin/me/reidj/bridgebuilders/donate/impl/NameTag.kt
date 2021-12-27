package me.func.commons.donate.impl

import dev.implario.bukkit.item.item
import me.func.commons.donate.DonatePosition
import me.func.commons.donate.MoneyFormatter
import me.func.commons.donate.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class NameTag(private val title: String, private val price: Int, private val rare: Rare) : DonatePosition {
    NONE("Отсутсвует", 0, Rare.COMMON),
    NEWBIE("Новичок", 128, Rare.COMMON),
    FAST("Молния", 480, Rare.COMMON),
    CAIN("Каин", 640, Rare.COMMON),
    NINJA("Ниндзя", 1280, Rare.RARE),
    EXECUTIONER("Палач", 1280, Rare.RARE),
    STRATEGIST("Стратег", 1600, Rare.RARE),
    SECRETAGENT("Тайный агент", 1600, Rare.RARE),
    INVESTIGATOR("Следователь", 2048, Rare.EPIC),
    KILLER("Киллер", 2048, Rare.EPIC),
    CHAMPION("Чемпион", 4096, Rare.LEGENDARY),
    LEGEND("Легенда", 4864, Rare.LEGENDARY);

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
        return item {
            type = Material.CLAY_BALL
            nbt("other", "pets1")
            text(rare.with(title) + "\n\n§fРедкость: ${rare.getColored()}\n§fСтоимость: ${MoneyFormatter.texted(price)}")
        }.build()
    }

    override fun give(user: User) {
        user.stat.activeNameTag = this
        user.stat.donate.add(this)
    }

    override fun isActive(user: User): Boolean {
        return user.stat.activeNameTag == this
    }

    override fun getName(): String {
        return name
    }

}