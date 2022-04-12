package me.reidj.bridgebuilders.content

import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.battlepass.BattlePass
import me.func.mod.battlepass.BattlePass.onBuyAdvanced
import me.func.mod.battlepass.BattlePass.onBuyPage
import me.func.mod.battlepass.BattlePass.sale
import me.func.mod.battlepass.BattlePassPageAdvanced
import me.func.mod.conversation.ModTransfer
import me.func.protocol.DropRare
import me.func.protocol.battlepass.BattlePassUserData
import me.reidj.bridgebuilders.battlepass.BattlePassUtil
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.util.Music
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage
import java.util.function.Consumer
import java.util.function.Function

const val BATTLEPASS_PRICE = 299
const val BATTLEPASS_SALE_PERCENT = 20

object BattlePassManager {

    private val premium = item {
        type = Material.CLAY_BALL
        text("§6Персонализация премиальный джойстик")
        nbt("p13nModelId", "e28387d9-c465-41a5-871b-7f27fd26076d")
        nbt("rare", DropRare.LEGENDARY.ordinal)
    }

    private val classic = item {
        type = Material.CLAY_BALL
        text("§bПерсонализация джойстик")
        nbt("p13nModelId", "1a4caaf5-77bc-4d7f-9302-6b2fcb510a6a")
        nbt("rare", DropRare.LEGENDARY.ordinal)
    }

    private val rewards = listOf(
        listOf(
            MoneyKit.SMALL,
            NameTag.BUILDER,
            MoneyKit.SMALL,
            NameTag.HARD_WORKER,
            LootboxUnit,
            MoneyKit.SMALL,
            NameTag.RALPH,
            MoneyKit.SMALL,
            LootboxUnit
        ) to listOf(
            NameTag.PAVEMENT,
            MoneyKit.SMALL,
            NameTag.MASON,
            MoneyKit.SMALL,
            LootboxUnit,
            NameTag.DESIGNER,
            MoneyKit.SMALL,
            Corpse.G1,
            MoneyKit.SMALL,
            LootboxUnit
        ),
        listOf(
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            NameTag.DROGBAR,
            LootboxUnit,
            MoneyKit.SMALL,
            KillMessage.AHEAD,
            MoneyKit.SMALL,
            StepParticle.SLIME,
            LootboxUnit
        ) to listOf(
            MoneyKit.SMALL,
            KillMessage.KIRA,
            MoneyKit.SMALL,
            LootboxUnit,
            NameTag.MUSKETEER,
            MoneyKit.SMALL,
            NameTag.CHIROPRACTOR,
            MoneyKit.SMALL,
            LootboxUnit
        ), listOf(
            MoneyKit.SMALL,
            StepParticle.VILLAGER_ANGRY,
            MoneyKit.SMALL,
            KillMessage.END,
            LootboxUnit,
            MoneyKit.SMALL,
            KillMessage.SLEEP,
            MoneyKit.SMALL,
            LootboxUnit
        ) to listOf(
            NameTag.ANARCHIST,
            MoneyKit.SMALL,
            NameTag.ARCHITECT,
            MoneyKit.SMALL,
            LootboxUnit,
            KillMessage.HORNY,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            LootboxUnit
        ),
        listOf(
            MoneyKit.SMALL,
            StepParticle.REDSTONE,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            LootboxUnit,
            MoneyKit.SMALL,
            Corpse.G2,
            MoneyKit.SMALL,
            LootboxUnit
        ) to listOf(
            MoneyKit.SMALL,
            StepParticle.VILLAGER_ANGRY,
            MoneyKit.NORMAL,
            LootboxUnit,
            StepParticle.VILLAGER_ANGRY,
            MoneyKit.SMALL,
            StarterKit.BLACKSMITH,
            MoneyKit.NORMAL,
            LootboxUnit
        ), listOf(
            MoneyKit.SMALL,
            StepParticle.FLAME,
            MoneyKit.SMALL,
            KillMessage.BLACK,
            LootboxUnit,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            LootboxUnit,
        ) to listOf(
            KillMessage.X,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            LootboxUnit,
            StarterKit.COOK,
            MoneyKit.SMALL,
            Corpse.G3,
            MoneyKit.SMALL,
            LootboxUnit
        ), listOf(
            MoneyKit.SMALL,
            Corpse.G4,
            MoneyKit.SMALL,
            StarterKit.MINER,
            LootboxUnit,
            MoneyKit.SMALL,
            StepParticle.NOTE,
            MoneyKit.SMALL,
            StarterKit.HEALER,
            LootboxUnit
        ) to listOf(
            KillMessage.ROOM,
            MoneyKit.SMALL,
            MoneyKit.SMALL,
            LootboxUnit,
            StarterKit.COLLECTOR,
            MoneyKit.SMALL,
            Corpse.G5,
            MoneyKit.SMALL,
            LootboxUnit
        )
    )

    private val battlepass = BattlePass.new(BATTLEPASS_PRICE) {
        pages = rewards.mapIndexed { index, drop ->
            BattlePassPageAdvanced(
                100 + 25 * index,
                10 + index,
                drop.first.map { it.getIcon() },
                drop.second.map { it.getIcon() }
            )
        }.toMutableList()
        sale(BATTLEPASS_SALE_PERCENT.toDouble())
        onBuyAdvanced { player ->
            val user = getByPlayer(player)?.stat
            player.closeInventory()

            user?.progress?.let { data ->
                if (data.advanced) {
                    Anime.itemTitle(player, CustomizationNPC.backItem, "Ошибка!", "У вас уже Премиум!", 2.5)
                    return@onBuyAdvanced
                }

                buy(
                    player,
                    BATTLEPASS_PRICE - (BATTLEPASS_PRICE * BATTLEPASS_SALE_PERCENT / 100.0).toInt(),
                    "Покупка премиум BattlePass'а."
                ) {
                    data.advanced = true
                    Anime.itemTitle(player, premium, "§bУспешно", "Собирайте награды!", 3.5)
                    Bukkit.getOnlinePlayers().forEach {
                        Anime.topMessage(
                            it,
                            Formatting.fine("§e${player.name} §fкупил §bПремиум §6BattlePass§f!")
                        )
                        Music.BONUS.sound(it)
                        it.sendMessage("")
                        it.sendMessage(Formatting.fine("§7Игрок §e${player.name} §7купил §bпремиум §6BattlePass§7!"))
                        it.sendMessage("")
                    }
                }
            }
        }
        onBuyPage { player, cost ->
            player.closeInventory()
            val user = getByPlayer(player)!!.stat

            user.progress?.let { data ->
                pages.firstOrNull { (it.skipPrice - it.skipPrice * BATTLEPASS_SALE_PERCENT / 100.0).toInt() == cost }
                    ?.let { page ->
                        buy(player, cost, "Пропуск уровня BattlePass.") {
                            data.exp += page.requiredExp
                            Anime.itemTitle(player, classic, "§bУспешно", "Новый уровень", 2.6)
                            Music.BONUS2.sound(player)
                            Bukkit.getOnlinePlayers().forEach {
                                Anime.topMessage(
                                    it,
                                    Formatting.fine("§e${player.name} §fпропустил страницу §6BattlePass§f!")
                                )
                            }
                        }
                    }
            }
        }

        facade.tags.add("Выполняйте квесты - получайте призы!")
        facade.tags.add("BattlePass завершится в 31.05.2022")
        questStatusUpdater = Function<Player, List<String>> { player ->
            BattlePassUtil.getQuestLore(player)
        }
    }

    init {
        Anime.createReader("bp:reward") { player, buffer ->
            val advanced = buffer.readBoolean()
            val page = buffer.readInt()
            val index = buffer.readInt()
            val data = getByPlayer(player)

            data!!.stat.progress?.let {
                if (advanced && !it.advanced)
                    return@createReader
                var exp = it.exp
                var level = 1

                for (current in battlepass.pages) {
                    for (item in current.items) {
                        if (exp >= current.requiredExp) {
                            exp -= current.requiredExp
                            level++
                        } else break
                    }
                }

                val position =
                    (if (advanced) battlepass.pages.size * battlepass.pages.first().items.size else 0) + page * 10 + index

                data.stat.claimedRewards?.let { claimed ->
                    if (claimed.contains(position))
                        return@createReader

                    if (level > page * 10 + index) {
                        val reward = (if (advanced) rewards[page].second else rewards[page].first)[index]

                        reward.give(data)
                        claimed.add(position)
                        Music.RARE_ITEM.sound(player)
                        Anime.killboardMessage(player, Formatting.fine("Награда: " + reward.getTitle()))
                    }
                }
            }
        }
    }

    private fun buy(player: Player, price: Int, desc: String, successfully: Consumer<Player>) {
        ISocketClient.get().writeAndAwaitResponse<MoneyTransactionResponsePackage>(
            MoneyTransactionRequestPackage(
                player.uniqueId,
                price,
                true,
                desc
            )
        ).thenAccept {
            if (it.errorMessage.isNullOrEmpty()) {
                successfully.accept(player)
            } else {
                Anime.itemTitle(player, CustomizationNPC.backItem, "Ошибка!", it.errorMessage, 2.5)
            }
        }
    }

    fun show(player: Player) {
        BattlePass.send(player, battlepass)
        val data = getByPlayer(player)!!.stat
        var progress = data.progress

        if (progress == null)
            progress = BattlePassUserData(5, false)

        // Back Door
        ModTransfer(battlepass.uuid.toString()).apply {
            integer(data.claimedRewards?.size ?: 0)
            data.claimedRewards?.forEach { integer(it) }
        }.send("bp:claimed", player)

        BattlePass.show(player, battlepass, progress)
    }
}