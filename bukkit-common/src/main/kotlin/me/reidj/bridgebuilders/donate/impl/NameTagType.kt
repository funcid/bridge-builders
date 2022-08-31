package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.data.Rare
import me.reidj.bridgebuilders.data.Rare.*
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class NameTagType(
    private val title: String,
    private val ether: Int,
    private val crystals: Int,
    private val rare: Rare
) : DonatePosition {
    NONE("§7Отсутствует", 0, 0, COMMON),
    // Платные префкисы
    DORA("§dДора", -1, 49, DONATE),
    COURSED("§cКурсед", -1, 49, DONATE),
    COOL("§5Крутышка", -1, 49, DONATE),
    SAKAMOTO("§eСакамото", -1, 49, DONATE),
    GHOUL_V2("§41000-7", -1, 49, DONATE),
    LELUSH("§6Лелуш", -1, 49, DONATE),
    WOMANIZER("§dБабник", -1, 49, DONATE),
    CLOWN("§eКлоун", -1, 49, DONATE),
    PODPIVASNIK("§eПодпивасник", -1, 49, DONATE),
    PEPSICOL("§6Пепсикольный", -1, 49, DONATE),
    BOOSTER("§bБустер", -1, 39, DONATE),
    HEART_BREAKER("§cСердцеедка", -1, 39, DONATE),
    TUCHENCIA("§eТученция", -1, 39, DONATE),
    VIN_DIESEL("§eВин Дизель", -1, 39, DONATE),
    POSEIDON("§bПосейдон", -1, 29, DONATE),
    YAKUZA("§cЯкудза", -1, 29, DONATE),
    SPY("§0Шпион", -1, 29, DONATE),
    // Бесплатные префиксы
    SCHOOLBOY("§7Школьник", 512, -1, COMMON),
    CAT("§7Кот", 512, -1, COMMON),
    CAT2("§7Кошка", 512, -1, COMMON),
    MOUSE("§7Мышь", 512, -1, COMMON),
    BUILDER("§7Строитель", 512, -1, COMMON),
    HARD_WORKER("§7Работяга", 512, -1, COMMON),
    ATHLETE("§7Физкультурник", 512, -1, COMMON),
    ANDESITE("§7Андезит", 512, -1, COMMON),
    WOOD("§7Дерево", 512, -1, COMMON),
    SAND("§7Песок", 512, -1, COMMON),
    STONE("§7Камень", 512, -1, COMMON),
    IRON("§7Утюг", 512, -1, COMMON),
    SOUP("§7Суп", 512, -1, COMMON),
    BALL("§7Шарик", 512, -1, COMMON),
    GOBLIN("§7Гоблин", 512, -1, COMMON),
    CARROT("§7Морковка", 512, -1, COMMON),
    BRIDGE("§aМостовой", 768, -1, UNUSUAL),
    DUDE("§aСтиляга", 768, -1, UNUSUAL),
    CRUSHER("§aКрушитель", 768, -1, UNUSUAL),
    HEDGEHOG("§aЕжик", 768, -1, UNUSUAL),
    VOID("§aПустой", 768, -1, UNUSUAL),
    MUSKETEER("§aМушкетёр", 768, -1, UNUSUAL),
    CHIROPRACTOR("§aКостоправ", 768, -1, UNUSUAL),
    PANTHER("§aПантера", 768, -1, UNUSUAL),
    METEOR("§aМетеор", 768, -1, UNUSUAL),
    RAGE("§aЯрость", 768, -1, UNUSUAL),
    CLAW("§aКоготь", 768, -1, UNUSUAL),
    EXCAVATOR("§aЗемлерой", 768, -1, UNUSUAL),
    DUELIST("§aДуэлист", 768, -1, UNUSUAL),
    SPIDER("§aПаук", 768, -1, UNUSUAL),
    TIGNARI("§9Тигнари", 1024, -1, RARE),
    SLIME("§9Слизь", 1024, -1, RARE),
    DEMON("§9Демон", 1024, -1, RARE),
    ARCHITECT("Архитектор", 1024, -1, RARE),
    EXPERIMENTER("§9Экспериментатор", 1024, -1, RARE),
    LONELY("§9Одинокий", 1024, -1, RARE),
    DESIGNER("§9Дизайнер", 1024, -1, RARE),
    DROGBAR("§9Дрогбар", 1024, -1, RARE),
    MINER("§9Майнер", 1024, -1, RARE),
    MODEST("§9Скромняга", 1024, -1, RARE),
    BIKER("§9Байкер", 1024, -1, RARE),
    PUNK("§9Панк", 1024, -1, RARE),
    SKUNK("§9Скунс", 1024, -1, RARE),
    HACKER("§9Хакер", 1024, -1, RARE),
    NUTCRACKER("§9Щелкунчик", 1024, -1, RARE),
    ACTOR("§9Актёр", 1024, -1, RARE),
    ACTRESS("§9Актриса", 1024, -1, RARE),
    VAMPIRE("§9Вампир", 1024, -1, RARE),
    MOWGLI("§5Маугли", 2048, -1, EPIC),
    ANARCHIST("§5Анархист", 2048, -1, EPIC),
    CRAZY("§5Псих", 2048, -1, EPIC),
    ITTO("§5Итто", 2048, -1, EPIC),
    DILYUK("§56Дилюк", 2048, -1, EPIC),
    HU_TAO("§5Ху Тао", 2048, -1, EPIC),
    JUNE_LI("§5Джун Ли", 2048, -1, EPIC),
    DEMON_SLAYER("§5Истребитель Демонов", 768, -1, EPIC),
    GHOUL("§5Гуль", 2048, -1, EPIC),
    DOTER("§5Дотер", 2048, -1, EPIC),
    ROMANTIC("§5Романтик", 2048, -1, EPIC),
    CHOPPER("§5Чоппер", 2048, -1, EPIC),
    MASON("§5Каменщик", 2048, -1, EPIC),
    RALPH("§5Ральф", 2048, -1, EPIC),
    SCP("§6SCP-096", 4096, -1, LEGENDARY),
    BIG_BOY("§6Биг Бой", 4096, -1, LEGENDARY),
    LEVY("§6Леви", 4096, -1, LEGENDARY),
    DART_VADER("§6Дарт Вейдер", 4096, -1, LEGENDARY),
    JOHN_WEEK("§6Джон Уик", 4096, -1, LEGENDARY),
    WALLY("§6Валли", 4096, -1, LEGENDARY),
    SHINIGAMI("§6Синигами", 4096, -1, LEGENDARY),
    IRIS("§6Айрис", 4096, -1, LEGENDARY),
    MINTO("§6Минто", 4096, -1, LEGENDARY),
    SHINON("§6Шинон", 4096, -1, LEGENDARY),
    JACK_SPARROW("§6Джек Воробей", 4096, -1, LEGENDARY),
    GUM("§6Жвачка", 4096, -1, LEGENDARY),
    ZEFIR("§6Зефир", 4096, -1, LEGENDARY),
    CIRCUS("§dЦиркач", 8192, -1, MYTHIC),
    RUINER("§dРуинер", 8192, -1, MYTHIC),
    AFKASHER("§dАфкашер", 8192, -1, MYTHIC),
    REPER("§dРепер", 8192, -1, MYTHIC),
    REPERSH("§dРеперша", 8192, -1, MYTHIC),
    HAYTER("§dХейтер", 8192, -1, MYTHIC),
    ENCODER("§dКодер", 8192, -1, MYTHIC),
    MARMALADE("§dМармеладка", 8192, -1, MYTHIC),
    EMO("§dЭмо", 8192, -1, MYTHIC),
    ;

    override fun getTitle() = title

    override fun getDescription(): String = ""

    override fun getEther() = ether

    override fun getCrystals() = crystals

    override fun getRare() = rare

    override fun getName() = name

    override fun getIcon() = item {
        type = Material.CLAY_BALL
        nbt("other", "pets1")
    }

    override fun getTexture(): String? = null

    override fun give(user: User) {
        user.stat.nameTags.add(name)
    }

    override fun isActive(user: User) = user.stat.currentNameTag == name
}