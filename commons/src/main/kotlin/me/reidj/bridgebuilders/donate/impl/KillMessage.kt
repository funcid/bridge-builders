package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class KillMessage(
    private val title: String,
    private val price: Int,
    private val rare: Rare,
    private val format: String
) :
    DonatePosition {

    NONE("Отсутствует", 0, Rare.COMMON, "убит"),
    GLOBAL("Средневековье", 64, Rare.COMMON, "прибит"),
    AHEAD("Замемлен", 128, Rare.COMMON, "был замемлен"),
    DEAD("Кусь", 256, Rare.COMMON, "укушен"),
    END("Галактический", 768, Rare.RARE, "превращен в космическую пыль"),
    SLEEP("Барбекю", 1024, Rare.RARE, "измазан в соусе барбекю"),
    HORNY("Огонь", 1024, Rare.RARE, "превращён в пыль"),
    ROOM("Насекомое", 2048, Rare.EPIC, "истреблён"),
    BLACK("Забычен", 2048, Rare.EPIC, "растоптан"),
    X("Банан", 8192, Rare.LEGENDARY, "очищен от кожуры"),
    KIRA("Компьютер", 8192, Rare.LEGENDARY, "был удалён"),
    ;

    override fun getTitle(): String {
        return title
    }

    override fun getDescription(): String = "§fПример: func $format игроком reidj"

    override fun getPrice(): Int {
        return price
    }

    override fun getRare(): Rare {
        return rare
    }

    fun getFormat(): String {
        return format
    }

    override fun getIcon(): ItemStack {
        return item {
            type = Material.CLAY_BALL
            nbt("other", "pets1")
        }.build()
    }

    override fun give(user: User) {
        user.stat.donates.add(getName())
    }

    override fun isActive(user: User): Boolean {
        return user.stat.activeKillMessage == data.KillMessage.valueOf(name)
    }

    override fun getName(): String {
        return name
    }

    override fun getObjectName(): String = name
}