package me.reidj.bridgebuilders.content

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.achievement.Achievement
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.donate.DonateHelper
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.realm
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.util.ParticleHelper
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import packages.SaveUserPackage
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.inventory.ClickableItem
import ru.cristalix.core.inventory.ControlledInventory
import ru.cristalix.core.inventory.InventoryContents
import ru.cristalix.core.inventory.InventoryProvider
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage
import ru.cristalix.core.realm.RealmStatus
import java.util.function.Consumer

object CustomizationNPC {

    val backItem = item {
        type = Material.CLAY_BALL
        text("§cНазад")
        nbt("other", "cancel")
    }.build()

    private val accessItem = item {
        text("§aКупить\n\n§7Это кнопка подтверждения\n§7покупки.")
        nbt("other", "access")
        enchant(Enchantment.LUCK, 1)
        type = Material.CLAY_BALL
    }.build()

    private val menu = ControlledInventory.builder()
        .title("BridgeBuilders")
        .rows(6)
        .columns(9)
        .provider(object : InventoryProvider {
            override fun init(player: Player, contents: InventoryContents) {
                contents.setLayout(
                    "XXXXXXXXX",
                    "XSXCXDXHX",
                    "XXXXXXXXX",
                    "XXZLKPIXX",
                    "XXXXOXXXX",
                    "XXXXQXXXX",
                )

                val user = app.getUser(player)!!
                val stat = user.stat
                contents.add('S', ClickableItem.empty(item {
                    type = Material.CLAY_BALL
                    nbt("other", "quest_week")
                    nbt("HideFlags", 63)
                    text(
                        "§f§l > §bОбщая статистика\n" +
                                "§7    Монет: §e${stat.money}\n" +
                                "§7    Лутбоксов: §b${stat.lootbox}\n" +
                                "§7    Награды: §f${stat.achievement.size}§7/${Achievement.values().size}\n" +
                                "§7    Лутбоксов открыто: §f${stat.lootboxOpenned}\n" +
                                "§7    Победы: §b${stat.wins}\n" +
                                "§7    Убийств: §c${stat.kills}\n" +
                                "§7    Сыграно: §f${stat.games} §7игр(ы)\n"
                    )
                }.build()))

                contents.add('P', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    nbt("other", "g2")
                    nbt("HideFlags", 63)
                    text("§bМогилы\n\n§7Выберите могилу, которая\n§7появится на месте\n§7вашей смерти.")
                }.build()) {
                    subInventory(player, 1) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout("XIIIIIXBX")
                        pasteItems(user, false, currentContent, Corpse.values().filter { it != Corpse.NONE }) {
                            user.stat.activeCorpse = data.Corpse.valueOf(it.objectName)
                        }
                    }
                })
                contents.add('K', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    nbt("other", "guild_members_add")
                    nbt("HideFlags", 63)
                    text("§bЧастицы ходьбы\n\n§7Выберите тип частиц,\n§7которые будут появлять\n§7следом за вами.")
                }.build()) {
                    subInventory(player, 3) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout(
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XXXXBXXXX"
                        )
                        pasteItems(user, false, currentContent, StepParticle.values().asIterable()) {
                            stat.activeParticle = data.StepParticle.valueOf(it.objectName)
                        }
                    }
                })
                contents.add('L', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    nbt("other", "new_booster_2")
                    nbt("HideFlags", 63)
                    text("§bПсевдонимы\n\n§7Выберите псевдоним,\n§7который появится в\n§7табе.")
                }.build()) {
                    subInventory(player, 3) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout(
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XXXXBXXXX"
                        )
                        pasteItems(user, false, currentContent, NameTag.values().asIterable()) {
                            stat.activeNameTag = data.NameTag.valueOf(it.objectName)
                        }
                    }
                })
                contents.add('C', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    enchant(Enchantment.LUCK, 0)
                    nbt("HideFlags", 63)
                    nbt("other", "new_lvl_rare_close")
                    text("§bМонеты\n\n§7Приобретите монеты,\n§7и ни в чем себе\n§7не отказывайте.")
                }.build()) {
                    subInventory(player, 1) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout("XIIIIXXBX")
                        pasteItems(user, true, currentContent, MoneyKit.values().filter { it != MoneyKit.NONE }) {}
                    }
                })
                contents.add('O', ClickableItem.of(LootboxUnit.getIcon()) {
                    donateMenu(player, LootboxUnit, false)
                })
                contents.add('I', ClickableItem.of(item {
                    type = Material.IRON_SPADE
                    nbt("simulators", "luck_shovel")
                    nbt("HideFlags", 63)
                    text("§bСообщения убийства\n\n§7Выберите сообщение,\n§7которое будет написано\n§7с 35% шансом, когда\n§7вы убьете кого-то.")
                }.build()) {
                    subInventory(player, 3) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout(
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XXXXBXXXX"
                        )
                        pasteItems(user, false, currentContent, KillMessage.values().asIterable()) {
                            stat.activeKillMessage = data.KillMessage.valueOf(it.objectName)
                        }
                    }
                })
                contents.add('Z', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    nbt("other", "bag")
                    nbt("HideFlags", 63)
                    text("§bСтартовые наборы\n\n§7Выберите набор, который \n§7поможет вам в игре.")
                }.build()) {
                    subInventory(player, 3) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout(
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XXXXBXXXX"
                        )
                        pasteItems(user, false, currentContent, StarterKit.values().asIterable()) {
                            stat.activeKit = data.StarterKit.valueOf(it.objectName)
                        }
                    }
                })

                contents.add('D', ClickableItem.of(StarterPack.getIcon()) {
                    donateMenu(player, StarterPack, true)
                })
                val countHaveAchievement =
                    Achievement.values()
                        .count { it.predicate(user) && !stat.achievement.contains(data.Achievement.valueOf(it.name)) }
                contents.add('H', ClickableItem.of(item {
                    type = Material.CLAY_BALL
                    nbt("other", "new_booster_1")
                    if (countHaveAchievement > 0)
                        text("§bДостижения\n\n§aВы можете собрать $countHaveAchievement наград!")
                    else
                        text("§bДостижения\n\n§7Посмотреть список достижений.")
                }.build()) {
                    subInventory(player, 5) { _: Player, currentContent: InventoryContents ->
                        currentContent.setLayout(
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XIIIIIIIX",
                            "XXXXBXXXX"
                        )
                        Achievement.values()
                            .sortedBy { !it.predicate(user) }
                            .sortedBy { stat.achievement.contains(data.Achievement.valueOf(it.name)) }
                            .forEach { oldAchievement ->
                                val achievement = data.Achievement.valueOf(oldAchievement.name)
                                val playerHas = stat.achievement.contains(achievement)
                                val canGet = oldAchievement.predicate(user)
                                currentContent.add('I', ClickableItem.of(item {
                                    type = Material.CLAY_BALL
                                    if (playerHas) {
                                        nbt("other", "new_booster_0")
                                        text("§aНаграда получена\n§7${oldAchievement.title}")
                                    } else {
                                        nbt("other", "new_booster_1")
                                        if (canGet && !playerHas) {
                                            enchant(Enchantment.DAMAGE_ALL, 1)
                                            text("§b${oldAchievement.title}\n\n${oldAchievement.lore}\n\n§aНажмите чтобы забрать награду!")
                                        } else {
                                            text("§b${oldAchievement.title}\n\n${oldAchievement.lore}")
                                        }
                                    }
                                }.build()) {
                                    if (!canGet || playerHas)
                                        return@of
                                    player.closeInventory()
                                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1f)
                                    oldAchievement.reward(user)
                                    user.stat.achievement.add(achievement)
                                    player.sendMessage(Formatting.fine("Вы успешно получили награду!"))
                                })
                            }
                    }
                })
                contents.add('Q', ClickableItem.of(backItem) { player.closeInventory() })
                contents.fillMask('X', ClickableItem.empty(ItemStack(Material.AIR)))
            }
        }).build()

    init {
        val npcLabel = worldMeta.getLabel("guide")
        // Создание подсветки NPC
        B.repeat(5) {
            if (realm.status == RealmStatus.GAME_STARTED_RESTRICTED)
                return@repeat
            ParticleHelper.happyVillager(npcLabel)
        }

        // Команда для открытия меню
        B.regCommand({ player, _ ->
            menu.open(player)
            null
        }, "menu", "help")
    }

    fun pasteItems(
        user: User,
        realMoney: Boolean,
        content: InventoryContents,
        item: Iterable<DonatePosition>,
        fill: (DonatePosition) -> Unit
    ) {
        item.forEach { currentItem ->
            content.add('I', ClickableItem.of(DonateHelper.modifiedItem(user, currentItem)) {
                if (user.stat.donates.contains(currentItem.objectName)) {
                    fill(currentItem)
                    user.player!!.closeInventory()
                    clientSocket.write(SaveUserPackage(user.stat.uuid, user.stat))
                } else {
                    donateMenu(user.player!!, currentItem, realMoney)
                }
            })
        }
    }

    fun subInventory(player: Player, rows: Int, inventory: (Player, InventoryContents) -> Any) {
        ControlledInventory.builder()
            .title("BridgeBuilders")
            .rows(rows)
            .columns(9)
            .provider(object : InventoryProvider {
                override fun init(player: Player, contents: InventoryContents) {
                    inventory(player, contents)
                    contents.fillMask('X', ClickableItem.empty(ItemStack(Material.AIR)))
                    contents.add('B', ClickableItem.of(backItem) { player.performCommand("menu") })
                }
            }).build()
            .open(player)
    }

    private fun donateMenu(player: Player, donatePosition: DonatePosition, realMoney: Boolean) {
        subInventory(player, 1) { _, contents: InventoryContents ->
            contents.setLayout("XOXXXXGBX")
            contents.add('O', ClickableItem.empty(donatePosition.getIcon()))
            contents.add('G', ClickableItem.of(accessItem) {
                val user = app.getUser(player)!!
                if (realMoney) {
                    buy(user, donatePosition.getPrice(), donatePosition.getTitle()) { donatePosition.give(user) }
                } else {
                    if (user.stat.donates.contains(donatePosition.objectName)) {
                        player.sendMessage(Formatting.error("У вас уже есть этот товар."))
                        player.closeInventory()
                    } else if (donatePosition.getPrice() > user.stat.money) {
                        player.sendMessage(Formatting.error("Не хватает денег :<"))
                        player.closeInventory()
                    } else {
                        user.minusMoney(donatePosition.getPrice())
                        donatePosition.give(user)
                        player.sendMessage(Formatting.fine("Успешно!"))
                        player.closeInventory()
                        clientSocket.write(SaveUserPackage(player.uniqueId, user.stat))
                    }
                }
            })
        }
    }

    private fun buy(user: User, money: Int, desc: String, accept: Consumer<User>) {
        val player = user.player!!
        ISocketClient.get().writeAndAwaitResponse<MoneyTransactionResponsePackage>(
            MoneyTransactionRequestPackage(player.uniqueId, money, true, desc)
        ).thenAccept {
            if (it.errorMessage != null) {
                player.sendMessage(Formatting.error(it.errorMessage))
                return@thenAccept
            }
            accept.accept(user)
            player.closeInventory()
            player.sendMessage(Formatting.fine("Спасибо за поддержку разработчиков!"))
            clientSocket.write(SaveUserPackage(player.uniqueId, user.stat))
        }
    }
}