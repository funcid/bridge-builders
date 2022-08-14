package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.donate.Rare.*
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.enchantments.Enchantment
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
    NONE("Отсутствует", 0, COMMON, BARRIER, null, "", arrayOf()),
    LUMBERJACK(
        "Лесоруб",
        256,
        COMMON,
        IRON_AXE,
        "weapons:iron_aztec_axe",
        "§bЖелезный топор, Кольчужный нагрудник, Яблоко х16",
        arrayOf(
            ItemStack(IRON_AXE),
            ItemStack(CHAINMAIL_CHESTPLATE),
            ItemStack(APPLE, 16)
        )
    ),
    EXCAVATOR(
        "Землерой",
        512,
        RARE,
        IRON_SPADE,
        "weapons:iron_aztec_shovel",
        "§bЖелезную лопату, Кольчужные поножи, Хлеб х16",
        arrayOf(
            ItemStack(IRON_SPADE),
            ItemStack(CHAINMAIL_LEGGINGS),
            ItemStack(BREAD, 16)
        )
    ),
    MINER(
        "Рудокоп",
        768,
        RARE,
        IRON_PICKAXE,
        "weapons:iron_aztec_pickaxe",
        "§bЖелезную кирку, Кольчужный шлем, Морковь х16",
        arrayOf(
            ItemStack(IRON_PICKAXE),
            ItemStack(CHAINMAIL_HELMET),
            ItemStack(CARROT_ITEM, 16)
        )
    ),
    BLACKSMITH(
        "Кузнец",
        768,
        RARE,
        DIAMOND_SWORD,
        "weapons:titan_axe",
        "§bНаковальня, Железо х8, Золото х16, Кольчужный сет, 16 яблок",
        arrayOf(
            ItemStack(ANVIL),
            ItemStack(IRON_INGOT, 8),
            ItemStack(GOLD_INGOT, 16),
            ItemStack(CHAINMAIL_HELMET),
            ItemStack(CHAINMAIL_CHESTPLATE),
            ItemStack(CHAINMAIL_LEGGINGS),
            ItemStack(CHAINMAIL_BOOTS),
            ItemStack(APPLE, 16)
        )
    ),
    COOK(
        "Дворф",
        2048,
        EPIC,
        DIAMOND_SWORD,
        "weapons:steel_axe",
        "§bАлмазный нагрудник (Защита III), Алмазные ботинки, Алмазная кирка, Алмазный топор (Острота II), Стейк х16",
        arrayOf(
            createEnchantItem(ItemStack(DIAMOND_CHESTPLATE), Enchantment.PROTECTION_ENVIRONMENTAL to 3),
            ItemStack(DIAMOND_BOOTS),
            ItemStack(DIAMOND_PICKAXE),
            createEnchantItem(ItemStack(DIAMOND_AXE), Enchantment.DAMAGE_ALL to 2),
            ItemStack(COOKED_BEEF, 16)
        )
    ),
    ASSASSIN(
        "Ассасин",
        2048,
        EPIC,
        IRON_SWORD,
        "weapons_other:42",
        "§bЗелье невидимости х2, Зелье скорости I x2, Лук, Стрелы х32, Каменный меч, Стейк х12, Удочка, Кольчужный сет (Защита I)",
        arrayOf(
            createPotion(PotionEffectType.INVISIBILITY, true, 15, 0, 2, "невидимости"),
            createPotion(PotionEffectType.SPEED, true, 60, 0, 2, "скорости"),
            ItemStack(BOW),
            ItemStack(ARROW, 32),
            ItemStack(STONE_SWORD),
            ItemStack(COOKED_BEEF, 12),
            ItemStack(FISHING_ROD),
            createEnchantItem(ItemStack(CHAINMAIL_HELMET), Enchantment.PROTECTION_ENVIRONMENTAL to 1),
            createEnchantItem(ItemStack(CHAINMAIL_CHESTPLATE), Enchantment.PROTECTION_ENVIRONMENTAL to 1),
            createEnchantItem(ItemStack(CHAINMAIL_LEGGINGS), Enchantment.PROTECTION_ENVIRONMENTAL to 1),
            createEnchantItem(ItemStack(CHAINMAIL_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL to 1),
        )
    ),
    HEALER(
        "Целитель",
        4096,
        LEGENDARY,
        CLAY_BALL,
        "other:heart",
        "§bЗелье регенерации II х3, Зелье лечения I х5, Золотое морковка х10, Железный сет",
        arrayOf(
            createPotion(PotionEffectType.REGENERATION, true, 60, 1, 3, "регенерации"),
            createPotion(PotionEffectType.HEAL, false, 0, 0, 5, "лечения"),
            ItemStack(GOLDEN_CARROT, 10),
            ItemStack(IRON_HELMET),
            ItemStack(IRON_CHESTPLATE),
            ItemStack(IRON_LEGGINGS),
            ItemStack(IRON_BOOTS)
        )
    ),
    ENCHANTER(
        "Репликон",
        4096,
        LEGENDARY,
        CLAY_BALL,
        "skyblock:yield",
        "Золотой сет, Золотой меч, Золотая морковка х16, Золотая кирка (Эффективность II), Золотой топор (Эффективность II), Золотая лопата (Эффективность 4)",
        arrayOf(
            ItemStack(GOLD_HELMET),
            ItemStack(GOLD_CHESTPLATE),
            ItemStack(GOLD_LEGGINGS),
            ItemStack(GOLD_BOOTS),
            ItemStack(GOLD_SWORD),
            ItemStack(GOLDEN_CARROT, 16),
            createEnchantItem(ItemStack(GOLD_PICKAXE), Enchantment.DIG_SPEED to 3),
            createEnchantItem(ItemStack(GOLD_AXE), Enchantment.DIG_SPEED to 3),
            createEnchantItem(ItemStack(GOLD_SPADE), Enchantment.DIG_SPEED to 4),
        )
    ),
    LUCIFER(
        "Люцифер",
        4096,
        LEGENDARY,
        IRON_SWORD,
        "bridgebuilders:lucifer",
        "§bОбсидиан х8, Алмазная кирка, Алмазный меч, Железный сет (Шипы I, Огнеупорность II), Зелье огнеустойкости х2, Золотая морковка х10",
        arrayOf(
            ItemStack(OBSIDIAN, 8),
            ItemStack(DIAMOND_PICKAXE),
            ItemStack(DIAMOND_SWORD),
            createEnchantItem(ItemStack(IRON_HELMET), Enchantment.THORNS to 1, Enchantment.PROTECTION_FIRE to 2 ),
            createEnchantItem(ItemStack(IRON_CHESTPLATE), Enchantment.THORNS to 1, Enchantment.PROTECTION_FIRE to 2 ),
            createEnchantItem(ItemStack(IRON_LEGGINGS), Enchantment.THORNS to 1, Enchantment.PROTECTION_FIRE to 2 ),
            createEnchantItem(ItemStack(IRON_BOOTS), Enchantment.THORNS to 1, Enchantment.PROTECTION_FIRE to 2 ),
            createPotion(PotionEffectType.FIRE_RESISTANCE, true, 30, 0, 2, "огнеуйстойкости"),
            ItemStack(GOLDEN_CARROT, 10)
        )
    ),
    PALADIN(
        "Паладин",
        4096,
        LEGENDARY,
        CLAY_BALL,
        "bridgebuilders:paladin",
        "§bАлмазный шлем (Защита II), Алмазные ботинки (Защита II), Железный нагрудник (Защита II), Железные штаны (Защита II), Золотые яблоки х6, Алмазный меч, Стейк х16, Зелье лечения х3",
        arrayOf(
            createEnchantItem(ItemStack(DIAMOND_HELMET), Enchantment.PROTECTION_ENVIRONMENTAL to 2),
            createEnchantItem(ItemStack(DIAMOND_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL to 2),
            createEnchantItem(ItemStack(IRON_CHESTPLATE), Enchantment.PROTECTION_ENVIRONMENTAL to 2),
            createEnchantItem(ItemStack(IRON_LEGGINGS), Enchantment.PROTECTION_ENVIRONMENTAL to 2),
            ItemStack(GOLDEN_APPLE, 6),
            ItemStack(DIAMOND_SWORD),
            ItemStack(COOKED_BEEF, 16),
            createPotion(PotionEffectType.HEAL, true, 1, 0, 3, "лечения")
        )
    ),
    COLLECTOR(
        "Коллекционер",
        8192,
        UNIQUE,
        GOLD_PICKAXE,
        "simulators:donate_pickaxe",
        "§bАлмазная кирка, Железная лопата, Каменный топор, Алмазный нагрудник, Железные штаны, Кольчужные ботинки, Хлеб х10",
        arrayOf(
            ItemStack(DIAMOND_PICKAXE),
            ItemStack(IRON_SPADE),
            ItemStack(STONE_AXE),
            ItemStack(DIAMOND_CHESTPLATE),
            ItemStack(IRON_LEGGINGS),
            ItemStack(CHAINMAIL_BOOTS),
            ItemStack(BREAD, 10)
        )
    ),
    SOUL_CATCHER(
        "Ловец Душ",
        8192,
        UNIQUE,
        IRON_SWORD,
        "weapons_other:43",
        "§bЗелье яда х3, Зелье моментального урона х2, Зелье замедления х3, Огниво, Кольчужный сет, Алмазный меч, Зелье силы I х1, Золотое яблоко х2, Золотая морковка х10",
        arrayOf(
            createPotion(PotionEffectType.POISON, true, 30, 0, 3, "отравления"),
            createPotion(PotionEffectType.HARM, true, 1, 0, 2, "урона"),
            createPotion(PotionEffectType.SLOW, true, 15, 0, 3, "замедления"),
            ItemStack(FLINT_AND_STEEL),
            ItemStack(CHAINMAIL_HELMET),
            ItemStack(CHAINMAIL_CHESTPLATE),
            ItemStack(CHAINMAIL_LEGGINGS),
            ItemStack(CHAINMAIL_BOOTS),
            ItemStack(DIAMOND_SWORD),
            createPotion(PotionEffectType.INCREASE_DAMAGE, false, 45, 0, 1, "силы"),
            ItemStack(GOLDEN_APPLE, 2),
            ItemStack(GOLDEN_CARROT, 10)
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
        return user.stat.activeKit == me.reidj.bridgebuilders.data.StarterKit.valueOf(name)
    }

    override fun getName() = name

    override fun getObjectName(): String = name
}

private fun createPotion(
    type: PotionEffectType,
    splash: Boolean,
    duration: Int,
    amplifier: Int,
    amount: Int,
    title: String
) = ItemStack(if (splash) SPLASH_POTION else POTION, amount).apply {
    itemMeta = (itemMeta as PotionMeta).apply {
        displayName = "Зелье $title"
        color = type.color
        addCustomEffect(PotionEffect(type, duration * 20, amplifier), true)
    }
}

private fun createEnchantItem(itemStack: ItemStack, vararg enchantment: Pair<Enchantment, Int>) =
    itemStack.apply { enchantment.forEach { addEnchantment(it.first, it.second) } }
