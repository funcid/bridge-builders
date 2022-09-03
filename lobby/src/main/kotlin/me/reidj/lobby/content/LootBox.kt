package me.reidj.lobby.content

import clepto.bukkit.B
import implario.humanize.Humanize
import me.func.mod.Anime
import me.func.mod.Banners
import me.func.mod.Banners.shineBlocks
import me.func.mod.data.LootDrop
import me.func.mod.selection.button
import me.func.mod.selection.selection
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.data.LootBoxType
import me.reidj.bridgebuilders.data.LootBoxType.*
import me.reidj.bridgebuilders.data.Rare
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.error
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.worldMeta
import me.reidj.lobby.ticker.Ticked
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import ru.cristalix.core.formatting.Formatting

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class LootBox : Listener, Ticked {

    private val banners = worldMeta.getLabels("lootbox").map {
        Banners.new {
            x = it.x + 0.5
            y = it.y + 3.6
            z = it.z
            motionSettings = hashMapOf(
                "yaw" to it.tag.split(" ")[0],
                "pitch" to 0.0
            )
            weight = 100
            height = 25
            shineBlocks(false)
        }
    }

    private val menu = selection {
        title = "Ваши лутбоксы"
        hint = "Открыть"
        rows = 3
        vault = "ruby"
        columns = 3
    }

    private val dropList = listOf<DonatePosition>()
        .asSequence()
        .plus(GraveType.values())
        .plus(MessageType.values())
        .plus(NameTagType.values())
        .plus(StartingKit.values())
        .plus(WalkingEffectType.values())
        .plus(LootBoxKit.values())
        .plus(MoneyKitType.values())
        .filter { it != GraveType.NONE && it != MessageType.NONE && it != NameTagType.NONE && it != StartingKit.NONE && it != WalkingEffectType.NONE }
        .toList()

    @EventHandler
    fun InventoryOpenEvent.handle() {
        if (inventory.type == InventoryType.ENDER_CHEST) {
            isCancelled = true
            val player = player as Player
            val user = getUser(player) ?: return
            val stat = user.stat
            menu.money = "Эфира ${stat.ether}"
            menu.storage = stat.lootBoxes.map {
                val box = it.lootBox
                button {
                    texture = "minecraft:mcpatcher/cit/simulators/${it.name.lowercase()}.png"
                    if (box.openPrice > 0)
                        price = box.openPrice.toLong()
                    title = "${box.rare.getColored()} лутбокс"
                    description = if (box.openLevel > 0) "Уровень для открытия ${box.openLevel}" else ""
                    onClick { player, _, _ ->
                        if (stat.ether < box.openPrice) {
                            Anime.close(player)
                            player.sendMessage(Formatting.error("Недостаточно средств!"))
                            player.error("Ошибка!", "Недостаточно средств")
                            return@onClick
                        } else if (user.getLevel() < box.openLevel) {
                            Anime.close(player)
                            player.sendMessage(Formatting.error("У Вас слишком низкий уровень!"))
                            player.error("Ошибка!", "Слишком низкий уровень")
                            return@onClick
                        }
                        Anime.close(player)
                        user.giveEther(-box.openPrice)
                        stat.lootBoxes.remove(it)
                        stat.lootBoxOpened++
                        open(user, player, it)
                    }
                }
            }.toMutableList()
            menu.open(player)
        }
    }

    fun open(user: User, player: Player?, boxType: LootBoxType) {
        val stat = user.stat
        val drop = when (boxType) {
            COMMON -> dropList.filter { it.getRare() == Rare.COMMON }
            UNUSUAL -> dropList.filter { it.getRare() == Rare.UNUSUAL }
            RARE -> dropList.filter { it.getRare() == Rare.RARE }
            EPIC -> dropList.filter { it.getRare() == Rare.EPIC }
            LEGENDARY -> dropList.filter { it.getRare() == Rare.LEGENDARY }
            else -> dropList.filter { it.getRare() == Rare.DONATE }
        }.random()

        Anime.openLootBox(player!!, LootDrop(drop.getIcon(), drop.getTitle(), drop.getRare().getTitle()))

        if (stat.donates.contains(drop.getName())) {
            val giveBack = (drop.getRare().ordinal + 1) * 48
            player.sendMessage(Formatting.fine("§aДубликат! §fЗаменен на §d$giveBack эфира§f."))
            user.giveEther(giveBack)
        } else {
            drop.give(user)
            stat.donates.add(drop.getName())
        }

        user.giveExperience(boxType.lootBox.experience)

        B.bc(Formatting.fine("§e${player.name} §fполучил §b${drop.getRare().with(drop.getTitle())}."))

        clientSocket.write(SaveUserPackage(stat.uuid, stat))
    }

    override fun tick(args: Int) {
        if (args % 30 == 0) {
            Bukkit.getOnlinePlayers().mapNotNull { getUser(it) }.forEach {
                val size = it.stat.lootBoxes.size
                it.cachedPlayer?.let { player ->
                    banners.forEach { banner ->
                        Banners.content(
                            player, banner.uuid, "§bЛутбокс\n§fДоступно $size ${
                                Humanize.plurals(
                                    "штука",
                                    "штуки",
                                    "штук",
                                    size
                                )
                            }"
                        )
                    }
                }
            }
        }
    }
}