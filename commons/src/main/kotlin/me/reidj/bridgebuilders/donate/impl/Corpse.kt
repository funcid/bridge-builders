package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class Corpse(private val title: String, private val price: Int, private val rare: Rare) : DonatePosition {

    NONE("Отсутствует", 0, Rare.COMMON),
    G1("Поломанная могила", 64, Rare.COMMON),
    G2("Скромная могила", 128, Rare.COMMON),
    G3("Монолит", 256, Rare.RARE),
    G4("Пямятник", 768, Rare.EPIC),
    G5("Монумент", 2048, Rare.LEGENDARY), ;

    override fun getTitle(): String {
        return title
    }

    override fun getDescription(): String = ""

    override fun getPrice(): Int {
        return price
    }

    override fun getRare(): Rare {
        return rare
    }

    override fun getIcon(): ItemStack {
        return item {
            type = Material.CLAY_BALL
            nbt("other", name.toLowerCase())
            text(rare.with(title) + "\n\n§fРедкость: ${rare.getColored()}\n§fСтоимость: ${MoneyFormatter.texted(price)}")
        }.build()
    }

    override fun give(user: User) {
        user.stat.donates.add(getName())
    }

    override fun isActive(user: User): Boolean {
        return user.stat.activeCorpse == data.Corpse.valueOf(objectName)
    }

    override fun getName(): String {
        return name
    }

    override fun getObjectName(): String = name
}