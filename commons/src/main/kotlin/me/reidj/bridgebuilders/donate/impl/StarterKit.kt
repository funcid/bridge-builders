package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

enum class StarterKit(
    private val title: String,
    private val price: Int,
    private val rare: Rare,
    private val icon: Material,
    private val tag: String?,
    private val lore: String,
    val content: Array<ItemStack>
) : DonatePosition {
    NONE("Отсутствует", 0, Rare.COMMON, Material.BARRIER, null, "", arrayOf()),
    LUMBERJACK(
        "Лесоруб",
        256,
        Rare.COMMON,
        Material.IRON_AXE,
        "weapons:iron_aztec_axe",
        "§bЖелезный топор, §bКольчужный нагрудник, §bЯблоко х16",
        arrayOf(
            ItemStack(Material.IRON_AXE),
            ItemStack(Material.CHAINMAIL_CHESTPLATE),
            ItemStack(Material.APPLE, 16)
        )
    ),
    EXCAVATOR(
        "Землерой",
        512,
        Rare.RARE,
        Material.IRON_SPADE,
        "weapons:iron_aztec_shovel",
        "§bЖелезную лопату, §bКольчужные поножи, §bХлеб х16",
        arrayOf(
            ItemStack(Material.IRON_SPADE),
            ItemStack(Material.CHAINMAIL_LEGGINGS),
            ItemStack(Material.BREAD, 16)
        )
    ),
    MINER(
        "Рудокоп",
        768,
        Rare.RARE,
        Material.IRON_PICKAXE,
        "weapons:iron_aztec_pickaxe",
        "§bЖелезную кирку, §bКольчужный шлем, §bМорковь х6",
        arrayOf(
            ItemStack(Material.IRON_PICKAXE),
            ItemStack(Material.CHAINMAIL_HELMET),
            ItemStack(Material.CARROT_ITEM, 6)
        )
    ),
    BLACKSMITH(
        "Кузнец",
        2048,
        Rare.EPIC,
        Material.DIAMOND_SWORD,
        "weapons:titan_axe",
        "§bПечка х2, §bЖелезная руда х16, §bУголь х16, §bЗолотой слиток х16, §bАлмаз х2, §bЖареная баранина х2",
        arrayOf(
            ItemStack(Material.FURNACE, 2),
            ItemStack(Material.IRON_ORE, 16),
            ItemStack(Material.COAL, 16),
            ItemStack(Material.GOLD_INGOT, 16),
            ItemStack(Material.DIAMOND, 2),
            ItemStack(Material.COOKED_MUTTON, 10)
        )
    ),
    ENCHANTER(
        "Зачарователь",
        2048,
        Rare.EPIC,
        Material.CLAY_BALL,
        "skyblock:yield",
        "§bКниги х16, §bНаковальня, §bКнижные полки х18, §bСтол зачарования, §bБутыльки опыта х256, §bБлоки лазурита х7, §bХлеб х16",
        arrayOf(
            ItemStack(Material.BOOK, 16),
            ItemStack(Material.ANVIL),
            ItemStack(Material.BOOKSHELF, 18),
            ItemStack(Material.ENCHANTMENT_TABLE),
            ItemStack(Material.EXP_BOTTLE, 64),
            ItemStack(Material.EXP_BOTTLE, 64),
            ItemStack(Material.EXP_BOTTLE, 64),
            ItemStack(Material.EXP_BOTTLE, 64),
            ItemStack(Material.LAPIS_BLOCK, 7),
            ItemStack(Material.BREAD, 16)
        )
    ),
    COOK(
        "Повар",
        2048,
        Rare.EPIC,
        Material.CAKE,
        null,
        "§bХлеб х32, §bЯблоко х32, §bТорт х5, §bЖареная баранина х16, §bСтейк х16",
        arrayOf(
            ItemStack(Material.BREAD, 32),
            ItemStack(Material.APPLE, 32),
            ItemStack(Material.CAKE, 5),
            ItemStack(Material.COOKED_MUTTON, 16),
            ItemStack(Material.COOKED_BEEF, 16)
        )
    ),
    HEALER(
        "Целитель",
        2048,
        Rare.EPIC,
        Material.CLAY_BALL,
        "other:heart",
        "§bЗелье регенерации I х2, §bЗелье лечения I х3, §bЗолотое яблоко х2, §bХлеб х16",
        arrayOf(
            createPotion(PotionEffectType.REGENERATION, 60, 0, 2, "регенерации"),
            createPotion(PotionEffectType.HEAL, 0, 0, 3, "лечения"),
            ItemStack(Material.GOLDEN_APPLE, 2),
            ItemStack(Material.BREAD, 16)
        )
    ),
    ASSASSIN(
        "Ассасин",
        2048,
        Rare.EPIC,
        Material.IRON_SWORD,
        "weapons_other:42",
        "§bЗелье невидимости х2, §bЗелье скорости I x2, §bЛук, §bСтрелы х32, §bКаменный меч, §bСтейк х12",
        arrayOf(
            createPotion(PotionEffectType.INVISIBILITY, 15, 0, 2, "невидимости"),
            createPotion(PotionEffectType.SPEED, 60, 0, 2, "скорости"),
            ItemStack(Material.BOW),
            ItemStack(Material.ARROW, 32),
            ItemStack(Material.STONE_SWORD),
            ItemStack(Material.COOKED_BEEF, 12)
        )
    ),
    COLLECTOR(
        "Коллекционер",
        4096,
        Rare.LEGENDARY,
        Material.GOLD_PICKAXE,
        "simulators:donate_pickaxe",
        "§bАлмазная кирка, §bАлмазный топор, §bАлмазная лопата, §bЖареная баранина х16",
        arrayOf(
            ItemStack(Material.DIAMOND_PICKAXE),
            ItemStack(Material.DIAMOND_AXE),
            ItemStack(Material.DIAMOND_SPADE),
            ItemStack(Material.COOKED_MUTTON, 16)
        )
    ),
    LUCIFER(
        "Люцифер",
        4096,
        Rare.LEGENDARY,
        Material.IRON_SWORD,
        "weapons_other:evil_trident",
        "§bКольчужный шлем, §bКольчужный нагрудник, §bКольчужные поножи, §bКольчужные ботинки, §bЖелезный меч, §bЗолотое яблоко х2",
        arrayOf(
            ItemStack(Material.CHAINMAIL_HELMET),
            ItemStack(Material.CHAINMAIL_CHESTPLATE),
            ItemStack(Material.CHAINMAIL_LEGGINGS),
            ItemStack(Material.CHAINMAIL_BOOTS),
            ItemStack(Material.IRON_SWORD),
            ItemStack(Material.GOLDEN_APPLE, 2)
        )
    ),
    PALADIN(
        "Паладин",
        4096,
        Rare.LEGENDARY,
        Material.IRON_SWORD,
        "weapons_other:wood_staff",
        "§bАлмазный меч, §bЖелезную кирку, §bЗолотая морковь х24, §bЗолотые яблоки х4, §bАлмазные ботинки",
        arrayOf(
            ItemStack(Material.DIAMOND_SWORD),
            ItemStack(Material.IRON_PICKAXE),
            ItemStack(Material.GOLDEN_CARROT, 24),
            ItemStack(Material.GOLD_CHESTPLATE),
            ItemStack(Material.GOLD_LEGGINGS),
            ItemStack(Material.DIAMOND_BOOTS),
            ItemStack(Material.GOLDEN_APPLE, 4)
        )
    ),
    ;

    override fun getTitle() = title

    override fun getDescription(): String = lore

    override fun getPrice() = price

    override fun getRare() = rare

    override fun getIcon(): ItemStack {
        return item {
            type = icon
            tag?.let {
                val pair = it.split(":")
                nbt(pair[0], pair[1])
            }
            text(
                rare.with("набор $title") + "\n\n§fРедкость: ${rare.getColored()}\n§fСтоимость: ${
                    MoneyFormatter.texted(
                        price
                    )
                } ${if (lore == "") "" else "\n\n§fВы получите:\n$lore"}"
            )
        }.build()
    }

    override fun give(user: User) {
        user.stat.donates.add(getName())
    }

    override fun isActive(user: User): Boolean {
        return user.stat.activeKit == data.StarterKit.valueOf(name)
    }

    override fun getName() = name

    override fun getObjectName(): String = name
}

fun createPotion(type: PotionEffectType, duration: Int, amplifier: Int, amount: Int, title: String): ItemStack {
    val potion = ItemStack(Material.SPLASH_POTION, amount)
    val potionMeta = potion.itemMeta as PotionMeta

    potionMeta.displayName = "Зелье $title"
    potionMeta.color = type.color
    potionMeta.addCustomEffect(PotionEffect(type, duration * 20, amplifier), true)
    potion.itemMeta = potionMeta

    return potion
}