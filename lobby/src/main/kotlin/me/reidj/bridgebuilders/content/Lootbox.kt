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
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.ticker.Ticked
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

private const val LOOT_BOX_PRICE = 192

class Lootbox : Listener, Ticked {

    private val location = worldMeta.getLabel("lootbox")
    private val banner = Banners.new {
        x = location.x + 0.5
        y = location.y + 3.6
        z = location.z
        weight = 100
        height = 25
        shineBlocks(false)
    }

    private val dropList = Corpse.values().map { it }
        .plus(NameTag.values())
        .plus(StepParticle.values())
        .plus(KillMessage.values())
        .plus(StarterKit.values())
        .filter { it != KillMessage.NONE && it != Corpse.NONE && it != NameTag.NONE && it != StepParticle.NONE && it != StarterKit.NONE }

    private val lootboxItem = item {
        type = Material.CLAY_BALL
        nbt("other", "enderchest1")
        text(
            "§bЛутбокс\n\n§7Откройте и получите\n§7псевдоним, частицы ходьбы\n§7следы от стрелы, маски\n§7или скин могилы!\n\n§e > §f㜰 §aОткрыть сейчас за\n${
                MoneyFormatter.texted(
                    LOOT_BOX_PRICE
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
                    price = LOOT_BOX_PRICE.toLong()
                    title = "§bЛутбокс"
                    onClick { player, _, _ ->
                        if (user.stat.money < LOOT_BOX_PRICE) {
                            player.sendMessage(Formatting.error("Не хватает монет :("))
                            return@onClick
                        }
                        Anime.close(player)
                        user.minusMoney(LOOT_BOX_PRICE)
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
                            user.giveMoney(giveBack, true)
                        } else {
                            drop.give(user)
                        }
                        user.giveMoney(moneyDrop, true)

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

    override fun tick(vararg args: Int) {
        if (args[0] % 30 == 0) {
            Bukkit.getOnlinePlayers().mapNotNull(getByPlayer).forEach {
                Banners.content(
                    it.player!!, banner.uuid, "§bЛутбокс\n§fДоступно ${it.stat.lootbox} ${
                        Humanize.plurals(
                            "штука",
                            "штуки",
                            "штук",
                            it.stat.lootbox
                        )
                    }\n"
                )
            }
        }
    }
}