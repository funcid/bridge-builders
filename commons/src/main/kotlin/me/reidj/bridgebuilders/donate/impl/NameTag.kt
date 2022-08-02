package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.donate.Rare.*
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class NameTag(private val title: String, private val price: Int, private val rare: Rare) : DonatePosition {
    NONE("Отсутствует", 0, COMMON),
    SCHOOLBOY("Школьник", 128, COMMON),
    CUTIE("Милашка", 128, COMMON),
    CAT("Кот", 128, COMMON),
    CATS("Кошка", 128, COMMON),
    MOUSE("Мышь", 128, COMMON),
    BUILDER("Строитель", 128, COMMON),
    HARD_WORKER("Работяга", 480, COMMON),
    PAVEMENT("Мостовой", 640, COMMON),
    STIL("Стиляга", 832, RARE),
    CRASHER("Крушитель", 832, RARE),
    MODEST("Скромняга", 832, RARE),
    MINER("Майнер", 832, RARE),
    ROMANTIC("Романтик", 832, RARE),
    LONELY("Одинокий", 832, RARE),
    CHOOPER("Чоппер", 1280, RARE),
    MASON("Каменщик", 1280, RARE),
    RALPH("Ральф", 1280, RARE),
    DESIGNER("Дизайнер", 1280, RARE),
    DROGBAR("Дрогбар", 1280, RARE),
    SPORTSMAN("Физкультурник", 1280, RARE),
    PANTHER("Пантера", 1280, RARE),
    CHIROPRACTOR("Костоправ", 2048, EPIC),
    GHOUL("Гуль", 2048, EPIC),
    DOTA("Дотер", 2048, EPIC),
    ANDESITE("Андезит", 4096, EPIC),
    THREE("Дерево", 4096, EPIC),
    SAND("Песок", 4096, EPIC),
    MUSKETEER("Мушкетёр", 2048, EPIC),
    PSYCHO("Псих", 2048, EPIC),
    EXPERIMENTER("Экспериментатор", 2048, EPIC),
    HEDGEHOG("Ёжик", 2048, EPIC),
    ARCHITECT("Архитектор", 4096, LEGENDARY),
    ANARCHIST("Анархист", 4864, LEGENDARY),
    VALLI("Валли", 4864, LEGENDARY),
    DEMON("Демон", 4096, LEGENDARY),
    MOWGLI("Маугли", 4096, LEGENDARY),
    JOHN_WICK("Джон Уик", 4096, LEGENDARY),
    CIRCUS("Циркач", 4096, LEGENDARY),
    SLAYER("Истребитель демонов", 4096, LEGENDARY),
    SHINIGAMI("Синигами", 4096, LEGENDARY),
    EMPTY("Пустой", 4096, LEGENDARY),
    MUCUS("Слизь", 4096, LEGENDARY),
    LIL("Lil", 8192, UNIQUE),
    REPER("Репер", 8192, UNIQUE),
    TIGNARI("Тигнари", 8192, UNIQUE),
    DORA("Дора", 8192, UNIQUE),
    CODER("Кодер", 8192, UNIQUE),
    CURSED("Курсед", 8192, UNIQUE),
    SCP("SCP-096", 8192, UNIQUE),
    LEVY("Леви", 8192, UNIQUE),
    DARTH_VADER("Дарт-Вейдер", 8192, UNIQUE),
    SPINNDER("Крутышка", 8192, UNIQUE),
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