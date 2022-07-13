package me.reidj.bridgebuilders.content

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import implario.humanize.Humanize
import me.func.mod.Anime
import me.func.mod.Banners
import me.func.mod.Banners.shineBlocks
import me.func.mod.data.LootDrop
import me.func.mod.selection.button
import me.func.mod.selection.selection
import me.func.protocol.element.MotionType
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import packages.SaveUserPackage
import ru.cristalix.core.formatting.Formatting

object Lootbox : Listener {

    init {
        val location = worldMeta.getLabel("lootbox")
        val banner = Banners.new {
            x = location.x + 0.5
            y = location.y + 3.6
            z = location.z
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

    init {
        B.regCommand({ player, _ ->
            player.playSound(player.location, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 1f, 2f)
            null
        }, "lootboxsound")
    }

    private val menu = selection {
        title = "Ваши лутбоксы"
        hint = "Открыть"
        rows = 4
        columns = 5
    }

    @EventHandler
    fun InventoryOpenEvent.handle() {
        if (inventory.type == InventoryType.ENDER_CHEST) {
            isCancelled = true
            val player = player as Player
            val user = app.getUser(player)!!
            menu.money = "Монет ${user.stat.money}"
            menu.storage = MutableList(user.stat.lootbox) {
                button {
                    item = lootboxItem
                    price = lootboxPrice.toLong()
                    title = "§bЛутбокс"
                    onClick { player, _, _ ->
                        if (user.stat.money < lootboxPrice) {
                            player.sendMessage(Formatting.error("Не хватает монет :("))
                            return@onClick
                        }
                        Anime.close(player)
                        user.minusMoney(lootboxPrice)
                        user.stat.lootbox--
                        user.stat.lootboxOpenned++

                        val drop = dropList.random() as DonatePosition
                        val moneyDrop = (Math.random() * 20 + 10).toInt()

                        Anime.openLootBox(
                            player, LootDrop(
                                drop.getIcon(),
                                drop.getTitle(),
                                drop.getRare().title
                            )
                        )

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
                        clientSocket.write(SaveUserPackage(user.stat.uuid, user.stat))
                    }
                }
            }
            menu.open(player)
        }
    }
}