package me.reidj.bridgebuilders.content

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import implario.humanize.Humanize
import me.func.mod.Banners
import me.func.mod.Banners.shineBlocks
import me.func.protocol.element.MotionType
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.inventory.ClickableItem
import ru.cristalix.core.inventory.ControlledInventory
import ru.cristalix.core.inventory.InventoryContents
import ru.cristalix.core.inventory.InventoryProvider

object Lootbox : Listener {

    init {
        worldMeta.getLabels("lootbox").forEach {
            val banner = Banners.new {
                x = it.x - 2.5
                y = it.y + 3.6
                z = it.z
                weight = 100
                height = 25
                opacity = .62
                motionType = MotionType.CONSTANT
                shineBlocks(false)
            }
            B.repeat(20) {
                Bukkit.getOnlinePlayers().mapNotNull(getByPlayer).forEach { user ->
                    Banners.content(
                        user.player!!, banner.uuid, "§bЛутбокс\n§fДоступно ${user.stat.lootbox} ${
                            Humanize.plurals(
                                "штука",
                                "штуки",
                                "штук",
                                user.stat.lootbox
                            )
                        }\n"
                    )
                }
            }
        }
    }

    private val dropList = Corpse.values().map { it }
        .plus(NameTag.values())
        .plus(StepParticle.values())
        .plus(KillMessage.values())
        .plus(StarterKit.values())
        .filter { it != KillMessage.NONE && it != Corpse.NONE && it != NameTag.NONE && it != StepParticle.NONE && it != StarterKit.NONE }

    private const val lootboxPrice = 192

    private val lootboxItem = item {
        type = Material.CLAY_BALL
        nbt("other", "enderchest1")
        text(
            "§bЛутбокс\n\n§7Откройте и получите\n§7псевдоним, частицы ходьбы\n§7следы от стрелы, маски\n§7или скин могилы!\n\n§e > §f㜰 §aОткрыть сейчас за\n${
                MoneyFormatter.texted(
                    lootboxPrice
                )
            }"
        )
    }.build()

    private val lootbox = ControlledInventory.builder()
        .title("Ваши лутбоксы")
        .rows(5)
        .columns(9)
        .provider(object : InventoryProvider {
            override fun init(player: Player, contents: InventoryContents) {
                contents.setLayout(
                    "XOOOOOOOX",
                    "XOOOOOOOX",
                    "XOOOPOOOX",
                    "XOOOOOOOX",
                    "XOOOOOOOX",
                )

                val user = app.getUser(player)!!
                repeat(minOf(user.stat.lootbox, contents.size('O'))) {
                    contents.add('O', ClickableItem.of(lootboxItem) {
                        player.closeInventory()
                        if (user.stat.money < lootboxPrice) {
                            player.sendMessage(Formatting.error("Не хватает монет :("))
                            return@of
                        }
                        user.minusMoney(lootboxPrice)
                        user.stat.lootbox--
                        user.stat.lootboxOpenned++

                        val drop = dropList.random() as DonatePosition
                        val moneyDrop = (Math.random() * 20 + 10).toInt()

                        me.func.mod.conversation.ModTransfer(
                            2,
                            CraftItemStack.asNMSCopy(drop.getIcon()),
                            drop.getTitle(),
                            drop.getRare().name,
                            CraftItemStack.asNMSCopy(ItemStack(Material.GOLD_INGOT)),
                            "§e$moneyDrop монет",
                            ""
                        ).send("lootbox", player)

                        if (user.stat.donates.contains(drop.objectName)) {
                            val giveBack = (drop.getRare().ordinal + 1) * 48
                            player.sendMessage(Formatting.fine("§aДубликат! §fЗаменен на §e$giveBack монет§f."))
                            user.giveMoney(giveBack)
                        } else {
                            drop.give(user)
                        }
                        user.giveMoney(moneyDrop)

                        B.bc(
                            Formatting.fine(
                                "§e${player.name} §fполучил §b${
                                    drop.getRare().with(drop.getTitle())
                                }."
                            )
                        )
                    })
                }
                contents.add('P', ClickableItem.empty(item {
                    type = Material.CLAY_BALL
                    nbt("other", "anvil")
                    text("§bКак их получить?\n\n§7Побеждайте в игре,\n§7и с шансом §a5%\n§7вы получите §bлутбокс§7.")
                }.build()))
                contents.fillMask('X', ClickableItem.empty(ItemStack(Material.AIR)))
            }
        }).build()

    init {
        B.regCommand({ player, _ ->
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1f, 2f)
            null
        }, "lootboxsound")
    }

    @EventHandler
    fun InventoryOpenEvent.handle() {
        if (inventory.type == InventoryType.ENDER_CHEST) {
            isCancelled = true
            lootbox.open(player as Player)
        }
    }
}