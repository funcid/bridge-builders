package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class NameTag(private val title: String, private val price: Int, private val rare: Rare) : DonatePosition {
    NONE("Отсутствует", 0, Rare.COMMON),
    SCHOOLBOY("Школьник", 128, Rare.COMMON),
    CUTIE("Милашка", 128, Rare.COMMON),
    CAT("Кот", 128, Rare.COMMON),
    CATS("Кошка", 128, Rare.COMMON),
    MOUSE("Мышь", 128, Rare.COMMON),
    BUILDER("Строитель", 128, Rare.COMMON),
    HARD_WORKER("Работяга", 480, Rare.COMMON),
    PAVEMENT("Мостовой", 640, Rare.COMMON),
    STIL("Стиляга", 832, Rare.RARE),
    CRASHER("Крушитель", 832, Rare.RARE),
    MODEST("Скромняга", 832, Rare.RARE),
    MINER("Майнер", 832, Rare.RARE),
    ROMANTIC("Романтик", 832, Rare.RARE),
    LONELY("Одинокий", 832, Rare.RARE),
    CHOOPER("Чоппер", 1280, Rare.RARE),
    MASON("Каменщик", 1280, Rare.RARE),
    RALPH("Ральф", 1280, Rare.RARE),
    DESIGNER("Дизайнер", 1600, Rare.RARE),
    DROGBAR("Дрогбар", 1600, Rare.RARE),
    CHIROPRACTOR("Костоправ", 2048, Rare.EPIC),
    GHOUL("Гуль", 2048, Rare.EPIC),
    DOTA("Дотер", 2048, Rare.EPIC),
    ANDESITE("Андезит", 4096, Rare.EPIC),
    THREE("Дерево", 4096, Rare.EPIC),
    SAND("Песок", 4096, Rare.EPIC),
    MUSKETEER("Мушкетёр", 2048, Rare.EPIC),
    ARCHITECT("Архитектор", 4096, Rare.LEGENDARY),
    ANARCHIST("Анархист", 4864, Rare.LEGENDARY),
    VALLI("Валли", 4864, Rare.LEGENDARY),
    DEMON("Демон", 4096, Rare.LEGENDARY),
    SLAYER("Истребитель демонов", 4096, Rare.LEGENDARY),
    SHINIGAMI("Синигами", 4096, Rare.LEGENDARY),
    EMPTY("Пустой", 4096, Rare.LEGENDARY),
    MUCUS("Слизь", 4096, Rare.LEGENDARY),
    LIL("Lil", 4096, Rare.LEGENDARY),
    REPER("Репер", 4096, Rare.LEGENDARY),
    ;

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
            nbt("other", "pets1")
            text(rare.with("префикс $title") + "\n\n§fРедкость: ${rare.getColored()}\n§fСтоимость: ${MoneyFormatter.texted(price)}")
        }.build()
    }

    override fun give(user: User) {
        user.stat.donates.add(getName())
    }

    override fun isActive(user: User): Boolean {
        return user.stat.activeNameTag == data.NameTag.valueOf(name)
    }

    override fun getName(): String {
        return name
    }

    override fun getObjectName(): String = name

}