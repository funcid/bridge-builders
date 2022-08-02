package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.donate.Rare.*
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class KillMessage(
    private val title: String,
    private val price: Int,
    private val rare: Rare,
    private val format: String
) : DonatePosition {
    NONE("Отсутствует", 0, COMMON, "%e убит игроком %k"),
    CHOP("Отбивная", 512, COMMON, "Мясо %e было сорвано с костей игроком %k"),
    AQUARIUM("Аквариум", 512, COMMON, "%e спит с рыбами благодаря игроку %k"),
    GLOBAL("Средневековье", 512, COMMON, "%e прибит игроком %k"),
    AHEAD("Замемлен", 512, COMMON, "%e был замемлен игроком %k"),
    DEAD("Кусь", 512, COMMON, "%e укушен игроком %k"),
    SNAKE("Змея", 1024, RARE, "%e не мог найти противоядие благодаря игроку %k"),
    END("Галактический", 1024, RARE, "%e превращен в космическую пыль игроком %k"),
    ASTRONAUT("Космонавт", 1024, RARE, "%e стал жертвой гравитации благодаря игроку %k"),
    GUILLOTINE("Гильотина", 1024, RARE, "%e потерял свою голову благодаря игроку %k"),
    SLEEP("Барбекю", 1024, RARE, "%e измазан в соусе барбекю игроком %k"),
    HORNY("Огонь", 1024, RARE, "%e превращён в пыль игроком %k"),
    ROOM("Насекомое", 2048, EPIC, "%e истреблён игроком %k"),
    BLACK("Забычен", 2048, EPIC, "%e растоптан игроком %k"),
    JAWS("Челюсти", 2048, EPIC, "%e стал акульей едой благодаря игроку %k"),
    EXECUTIONER("Палач", 2048, EPIC, "%e молил о смерти получил её благодаря игроку %k"),
    LIFE_DRAINER("Высасыватель жизни", 2048, EPIC, "Жизненно важные органы %e были разорваны игроком %k"),
    X("Банан", 8192, LEGENDARY, "%e очищен от кожуры игроком %k"),
    KIRA("Компьютер", 8192, LEGENDARY, "%e был удалён игроком %k"),
    TIME_MACHINE("Машина времени", 8192, LEGENDARY, "Похождение %e наконец-то было остановлено игроком %k"),
    SURGEON("Хирург", 8192, LEGENDARY, "%k видел как внутренности %e стали внешностями"),
    PUPPETEER("Кукловод", 8192, LEGENDARY, "Ноги %e появились там, где должна быть его голова благодаря игроку %k"),
    ;

    override fun getTitle(): String {
        return title
    }

    override fun getDescription(): String = "§fПример: ${format.replace("%e", "func").replace("%k", "reidj")}"

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