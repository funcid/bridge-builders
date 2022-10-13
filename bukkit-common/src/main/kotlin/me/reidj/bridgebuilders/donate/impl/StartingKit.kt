package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.data.Rare
import me.reidj.bridgebuilders.data.Rare.*
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material.*
import org.bukkit.enchantments.Enchantment.*
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class StartingKit(
    private val title: String,
    private val description: String,
    private val texture: String,
    private val ether: Int,
    private val crystals: Int,
    private val level: Int,
    private val rare: Rare,
    val armorContent: Array<ItemStack>,
    vararg val inventory: ItemStack
) : DonatePosition {
    NONE(
        "Стандартный",
        "Кожаный сет\nДеревянные инструменты\nХлеб х32",
        "default",
        0,
        0,
        0,
        COMMON,
        arrayOf(
            ItemStack(LEATHER_BOOTS),
            ItemStack(LEATHER_LEGGINGS),
            ItemStack(LEATHER_CHESTPLATE),
            ItemStack(LEATHER_HELMET)
        ),
        ItemStack(WOOD_SWORD),
        ItemStack(WOOD_PICKAXE),
        ItemStack(WOOD_AXE),
        ItemStack(WOOD_SPADE),
        ItemStack(BREAD, 32)
    ),
    LUMBERJACK(
        "Лесоруб",
        "Железный топор\nКольчужный нагрудник\nЯблоко х16",
        "lesorub",
        512,
        0,
        0,
        COMMON,
        arrayOf(ItemStack(AIR), ItemStack(AIR), ItemStack(CHAINMAIL_CHESTPLATE), ItemStack(AIR)),
        ItemStack(IRON_AXE),
        ItemStack(APPLE, 16)
    ),
    EXCAVATOR(
        "Землерой", "Железная лопата\nКольчужные поножи\nХлеб х16", "zemleroi", 512, 0, 0, COMMON, arrayOf(
            ItemStack(AIR),
            ItemStack(CHAINMAIL_LEGGINGS),
            ItemStack(AIR),
            ItemStack(AIR)
        ),
        ItemStack(IRON_SPADE), ItemStack(BREAD, 16)
    ),
    MINER(
        "Рудокоп", "Железная кирка\nКольчужный шлем\nМорковь х16", "rudocop", 512, 0, 0, COMMON, arrayOf(
            ItemStack(AIR),
            ItemStack(AIR),
            ItemStack(AIR),
            ItemStack(CHAINMAIL_HELMET)
        ), ItemStack(IRON_PICKAXE), ItemStack(CARROT_ITEM, 16)
    ),
    BLACKSMITH(
        "Кузнец", "Кольчужный сет\nЖелезо х8\nЗолото х16\nЯблоки х16", "kuznec", 768, 0, 10, UNUSUAL, arrayOf(
            ItemStack(CHAINMAIL_BOOTS),
            ItemStack(CHAINMAIL_LEGGINGS),
            ItemStack(CHAINMAIL_CHESTPLATE),
            ItemStack(CHAINMAIL_HELMET)
        ), ItemStack(IRON_INGOT, 8), ItemStack(GOLD_INGOT, 16), ItemStack(APPLE, 16)
    ),
    GOBLIN(
        "Гоблин",
        "Кожаный сет\nДеревянный меч (Острота IV)\nДеревянные инструменты (Эффективность I)\nСтейк х5",
        "goblin",
        1024,
        0,
        10,
        UNUSUAL,
        arrayOf(
            ItemStack(LEATHER_BOOTS),
            ItemStack(LEATHER_LEGGINGS),
            ItemStack(LEATHER_CHESTPLATE),
            ItemStack(LEATHER_HELMET)
        ),
        item {
            type = WOOD_SWORD
            enchant(DAMAGE_ALL, 4)
        },
        item {
            type = WOOD_AXE
            enchant(DIG_SPEED, 3)
        },
        item {
            type = WOOD_SPADE
            enchant(DIG_SPEED, 3)
        },
        item {
            type = WOOD_PICKAXE
            enchant(DIG_SPEED, 3)
        },
        ItemStack(COOKED_BEEF, 5)
    ),
    DWORF(
        "Дворф",
        "Алмазный нагрудник (Защита I)\nАлмазные ботинки (Защита I)\nАлмазная кирка\nАлмазный топор (Острота II)\nСтейк х16",
        "dworf",
        1024,
        0,
        20,
        RARE,
        arrayOf(
            item {
                type = DIAMOND_BOOTS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            ItemStack(AIR),
            item {
                type = DIAMOND_CHESTPLATE
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            ItemStack(AIR)
        ),
        ItemStack(DIAMOND_PICKAXE),
        item {
            type = DIAMOND_AXE
            enchant(DAMAGE_ALL, 2)
        },
        ItemStack(COOKED_BEEF, 16)
    ),
    ASSASSIN(
        "Ассасин",
        "Кольчужный сет (Защита I)\nЗелье невидимости х2\nЗелье скорости I x2\nЛук\nСтрелы х32\nКаменный меч\nСтейк х12\nУдочка",
        "assasin",
        1024,
        0,
        20,
        RARE,
        arrayOf(
            item {
                type = CHAINMAIL_BOOTS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = CHAINMAIL_LEGGINGS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = CHAINMAIL_CHESTPLATE
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = CHAINMAIL_HELMET
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
        ),
        item {
            type = WOOD_SWORD
            enchant(KNOCKBACK, 2)
        },
        createPotion(PotionEffectType.REGENERATION, true, 60, 1, 3, "регенерации"),
        createPotion(PotionEffectType.HEAL, false, 0, 0, 5, "лечения"),
        ItemStack(GOLDEN_CARROT, 10),
    ),
    ELF(
        "Эльф",
        "Кожаный сет (Защита II)\nЛук (Сила I, Откидывание I)\nСтрела х32\nЗолотое яблоко х3\nЯблоко х32",
        "elf",
        2048,
        0,
        20,
        RARE,
        arrayOf(
            item {
                type = LEATHER_BOOTS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = LEATHER_LEGGINGS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = LEATHER_CHESTPLATE
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = LEATHER_HELMET
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
        ), item {
            type = org.bukkit.Material.BOW
        }.apply {
            itemMeta = itemMeta.apply {
                addEnchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 1, true)
                addEnchant(org.bukkit.enchantments.Enchantment.KNOCKBACK, 1, true)
            }
        }, ItemStack(ARROW, 32), ItemStack(GOLDEN_APPLE, 3), ItemStack(APPLE, 32)
    ),
    HEALER(
        "Целитель",
        "Зелье регенерации II х3\nЗелье лечения I х5\nЗолотое морковка х10\nЖелезный сет (Защита I)\nДеревянный меч (Отдача II)",
        "celitel",
        1024,
        0,
        20,
        RARE,
        arrayOf(
            item {
                type = IRON_BOOTS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = IRON_LEGGINGS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = IRON_CHESTPLATE
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = IRON_HELMET
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
        ),
        item {
            type = WOOD_SWORD
            enchant(KNOCKBACK, 2)
        },
        createPotion(PotionEffectType.REGENERATION, true, 60, 1, 3, "регенерации"),
        createPotion(PotionEffectType.HEAL, false, 0, 0, 5, "лечения"),
        ItemStack(GOLDEN_CARROT, 10),
    ),
    URUKHAI(
        "Урукхай",
        "Кольчужный сет (Шипы II)\nЛук\nЯдовитая стрела х64\nЗелье скорости II\nЗолотое яблоко\nСтейк х16",
        "urukhai",
        4096,
        0,
        30,
        EPIC,
        arrayOf(
            item {
                type = CHAINMAIL_BOOTS
                enchant(THORNS, 1)
            },
            item {
                type = CHAINMAIL_LEGGINGS
                enchant(THORNS, 1)
            },
            item {
                type = CHAINMAIL_CHESTPLATE
                enchant(THORNS, 1)
            },
            item {
                type = CHAINMAIL_HELMET
                enchant(THORNS, 1)
            },
        ), item {
            type = BOW
        },
        item {
            text("Отравляющая стрела")
            type = org.bukkit.Material.TIPPED_ARROW
            amount(64)
        }.apply {
            itemMeta = (itemMeta as PotionMeta).apply {
                this.color = org.bukkit.Color.GREEN
                addCustomEffect(PotionEffect(PotionEffectType.POISON, 360, 0), true)
            }
        },
        createPotion(PotionEffectType.SPEED, true, 25, 0, 1, "скорости"),
        ItemStack(GOLDEN_APPLE),
        ItemStack(COOKED_BEEF, 16)
    ),
    LEPRECHAUN(
        "Лепрекон",
        "Золотой сет\nЗолотой меч\nЗолотая кирка (Эффективность II, Прочность III)\nЗолотой топор (Эффективность III, Прочность II)\n" +
                "Золотая лопата (Эффективность IV, Прочность I)\nЗолотая морковка х16,",
        "leprekon",
        2048,
        0,
        30,
        EPIC,
        arrayOf(
            ItemStack(GOLD_BOOTS),
            ItemStack(GOLD_LEGGINGS),
            ItemStack(GOLD_CHESTPLATE),
            ItemStack(GOLD_HELMET)
        ),
        ItemStack(GOLD_SWORD),
        ItemStack(GOLDEN_CARROT, 16),
        item {
            type = GOLD_PICKAXE
        }.apply {
            itemMeta = itemMeta.apply {
                addEnchant(DIG_SPEED, 3, true)
                addEnchant(DURABILITY, 2, true)
            }
        },
        item {
            type = GOLD_AXE
        }.apply {
            itemMeta = itemMeta.apply {
                addEnchant(DIG_SPEED, 3, true)
                addEnchant(DURABILITY, 2, true)
            }
        },
        item {
            type = org.bukkit.Material.GOLD_SPADE
        }.apply {
            itemMeta = itemMeta.apply {
                addEnchant(DIG_SPEED, 4, true)
                addEnchant(DURABILITY, 1, true)
            }
        },
    ),
    LUCIFER(
        "Люцифер",
        "Железный сет (Шипы I, Огнеупорность II)\nАлмазная кирка\nАлмазный меч\nЗелье огнеустойкости х2\nЗолотая морковка х10\nОбсидиан х8",
        "lucik",
        2048,
        0,
        30,
        EPIC,
        arrayOf(
            item {
                type = org.bukkit.Material.IRON_BOOTS
            }.apply {
                itemMeta = itemMeta.apply {
                    addEnchant(THORNS, 1, true)
                    addEnchant(FIRE_ASPECT, 2, true)
                }
            },
            item {
                type = org.bukkit.Material.IRON_LEGGINGS
            }.apply {
                itemMeta = itemMeta.apply {
                    addEnchant(THORNS, 1, true)
                    addEnchant(FIRE_ASPECT, 2, true)
                }
            },
            item {
                type = IRON_CHESTPLATE
            }.apply {
                itemMeta = itemMeta.apply {
                    addEnchant(THORNS, 1, true)
                    addEnchant(FIRE_ASPECT, 2, true)
                }
            },
            item {
                type = IRON_HELMET
            }.apply {
                itemMeta = itemMeta.apply {
                    addEnchant(THORNS, 1, true)
                    addEnchant(FIRE_ASPECT, 2, true)
                }
            },
        ),
        ItemStack(DIAMOND_SWORD),
        ItemStack(DIAMOND_PICKAXE),
        ItemStack(OBSIDIAN, 8),
        ItemStack(GOLDEN_CARROT, 10),
        createPotion(PotionEffectType.FIRE_RESISTANCE, true, 30, 0, 2, "огнеуйстойкости"),
    ),
    SOUL_CATCHER(
        "Ловец Душ",
        "Зелье яда х3\nЗелье моментального урона х2\nЗелье замедления х3\nОгниво\nКольчужный сет\n" +
                "Алмазный меч\nЗелье силы I х1\nЗолотое яблоко х2\nЗолотая морковка х10",
        "lovec_dush",
        2048,
        0,
        30,
        EPIC,
        arrayOf(
            ItemStack(CHAINMAIL_BOOTS),
            ItemStack(CHAINMAIL_LEGGINGS),
            ItemStack(CHAINMAIL_CHESTPLATE),
            ItemStack(CHAINMAIL_HELMET)
        ),
        ItemStack(DIAMOND_SWORD),
        createPotion(PotionEffectType.POISON, true, 30, 0, 3, "отравления"),
        createPotion(PotionEffectType.HARM, true, 1, 0, 2, "урона"),
        createPotion(PotionEffectType.SLOW, true, 15, 0, 3, "замедления"),
        ItemStack(FLINT_AND_STEEL),
        createPotion(PotionEffectType.INCREASE_DAMAGE, false, 45, 0, 1, "силы"),
        ItemStack(GOLDEN_APPLE, 2),
        ItemStack(GOLDEN_CARROT, 10)
    ),
    JUMPER(
        "Джампер",
        "Алмазные ботинки (Невесомость IV)\nЗелье прыгучести х3\nЛук (Откидывание II)\nСтрела х32\nЖелезная кирка (Эффективность II)",
        "jumper",
        8192,
        0,
        40,
        LEGENDARY,
        arrayOf(
            item {
                type = org.bukkit.Material.DIAMOND_BOOTS
                enchant(PROTECTION_FALL, 4)
            },
            ItemStack(AIR),
            ItemStack(AIR),
            ItemStack(AIR)
        ),
        item {
            type = org.bukkit.Material.BOW
            enchant(KNOCKBACK, 2)
        },
        ItemStack(ARROW, 32),
        createPotion(PotionEffectType.JUMP, false, 60 * 20, 1, 3, "прыгучести"),
        item {
            type = IRON_PICKAXE
            enchant(DIG_SPEED, 2)
        }
    ),
    PALADIN(
        "Паладин",
        "Алмазный шлем\nАлмазные ботинки\nЖелезный нагрудник\nЖелезные штаны\nЗолотые яблоки х2\nАлмазный меч\nСтейк х16\nЗелье лечения х3",
        "paladin",
        4096,
        0,
        40,
        LEGENDARY,
        arrayOf(
            ItemStack(DIAMOND_BOOTS),
            ItemStack(IRON_LEGGINGS),
            ItemStack(IRON_CHESTPLATE),
            ItemStack(DIAMOND_HELMET),
        ),
        ItemStack(DIAMOND_SWORD),
        ItemStack(GOLDEN_APPLE, 2),
        ItemStack(COOKED_BEEF, 16),
        createPotion(PotionEffectType.HEAL, true, 1, 0, 3, "лечения")
    ),
    COLLECTOR(
        "Коллекционер",
        "Алмазная кирка\nЖелезная лопата (Эффективность II)\nКаменный топор (Эффективность II)\n" +
                "Алмазный нагрудник\nЖелезные штаны\nКольчужные ботинки\nХлеб х10\nДеревянный меч (Острота III, Заговор огня II)",
        "collector",
        4096,
        0,
        40,
        LEGENDARY,
        arrayOf(
            ItemStack(CHAINMAIL_BOOTS),
            ItemStack(IRON_LEGGINGS),
            ItemStack(DIAMOND_CHESTPLATE),
            ItemStack(AIR)
        ),
        item {
            type = org.bukkit.Material.WOOD_SWORD
        }.apply {
            itemMeta = itemMeta.apply {
                addEnchant(DAMAGE_ALL, 3, true)
                addEnchant(FIRE_ASPECT, 2, true)
            }
        },
        ItemStack(DIAMOND_PICKAXE),
        item {
            type = org.bukkit.Material.IRON_SPADE
            enchant(DIG_SPEED, 2)
        },
        item {
            type = org.bukkit.Material.STONE_AXE
            enchant(DIG_SPEED, 2)
        },
        ItemStack(BREAD, 10)
    ),
    DRILLING_RIG(
        "Буровая установка",
        "Кольчужный сет (Защита I)\nЖелезная кирка (Эффективность III, Прочность III)\n" +
                "Золотая лопата (Эффективность IV, Прочность II)\nКаменный меч (Острота I)\nЗолотое яблоко х1\nСтейк х16",
        "bur",
        16384,
        0,
        50,
        MYTHIC,
        arrayOf(
            item {
                type = CHAINMAIL_BOOTS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = CHAINMAIL_LEGGINGS
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = CHAINMAIL_CHESTPLATE
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            },
            item {
                type = CHAINMAIL_HELMET
                enchant(PROTECTION_ENVIRONMENTAL, 1)
            }
        ), item {
            type = STONE_SWORD
            enchant(DAMAGE_ALL, 1)
        }, ItemStack(GOLDEN_APPLE, 1), ItemStack(COOKED_BEEF, 16),
        item {
            type = org.bukkit.Material.GOLD_SPADE
        }.apply {
            itemMeta = itemMeta.apply {
                addEnchant(DIG_SPEED, 4, true)
                addEnchant(DURABILITY, 2, true)
            }
        },
        item {
            type = org.bukkit.Material.IRON_PICKAXE
        }.apply {
            itemMeta = itemMeta.apply {
                addEnchant(DIG_SPEED, 3, true)
                addEnchant(DURABILITY, 3, true)
            }
        }
    )
    ;

    override fun getTitle() = "Набор $title"

    override fun getDescription(): String = description

    override fun getEther() = ether

    override fun getCrystals() = crystals

    override fun getRare() = rare

    override fun getName() = name

    override fun getLevel() = level

    override fun getIcon(): ItemStack {
        TODO("Not yet implemented")
    }

    override fun getTexture() = me.reidj.bridgebuilders.getTexture(texture)

    override fun give(user: User) {
        user.stat.startingKits.add(name)
    }

    override fun isActive(user: User): Boolean {
        return user.stat.currentStarterKit == name
    }
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

