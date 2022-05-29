package me.reidj.bridgebuilders.content

import clepto.bukkit.B
import data.Corpse
import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.selection.Button
import me.func.mod.selection.Confirmation
import me.func.mod.selection.button
import me.func.mod.selection.selection
import me.func.protocol.GlowColor
import me.reidj.bridgebuilders.achievement.Achievement
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.util.ParticleHelper
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import packages.SaveUserPackage
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.GetAccountBalancePackage
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage
import java.util.concurrent.TimeUnit

object CustomizationNPC {

    private fun <T : DonatePosition> temp(
        player: Player,
        name: String,
        isDonate: Boolean,
        vararg donate: T,
        converter: (Button, T) -> Button = { button, _ -> button }
    ) {
        selection {
            val user = app.getUser(player)!!
            val stat = user.stat
            title = name
            rows = 3
            columns = 3
            if (isDonate)
                vault = "donate"
            money = if (isDonate) {
                val balance = ISocketClient.get().writeAndAwaitResponse<GetAccountBalancePackage>(
                    GetAccountBalancePackage(player.uniqueId)
                ).get(1, TimeUnit.SECONDS).balanceData
                "Кристаликов ${balance.coins + balance.crystals}"
            } else {
                "Монет " + stat.money
            }
            storage = donate.map { pos ->
                converter(button {
                    val sale = if (pos is MoneyKit) pos.percent else 0
                    val has = when (pos) {
                        is me.reidj.bridgebuilders.donate.impl.Corpse -> stat.donates.contains(pos.objectName)
                        is KillMessage -> stat.donates.contains(pos.objectName)
                        is NameTag -> stat.donates.contains(pos.objectName)
                        is StarterKit -> stat.donates.contains(pos.objectName)
                        is StepParticle -> stat.donates.contains(pos.objectName)
                        else -> false
                    }
                    if (!has) price = pos.getPrice().toLong()
                    else hint = "Выбрать"
                    val current = has && when (pos) {
                        is me.reidj.bridgebuilders.donate.impl.Corpse -> stat.activeCorpse == Corpse.valueOf(pos.objectName)
                        is KillMessage -> stat.activeKillMessage == data.KillMessage.valueOf(pos.objectName)
                        is NameTag -> stat.activeNameTag == data.NameTag.valueOf(pos.objectName)
                        is StarterKit -> stat.activeKit == data.StarterKit.valueOf(pos.objectName)
                        is StepParticle -> stat.activeParticle == data.StepParticle.valueOf(pos.objectName)
                        else -> false
                    }
                    onClick { player, _, _ ->
                        if (current)
                            return@onClick
                        Anime.close(player)
                        if (has) {
                            when (pos) {
                                is me.reidj.bridgebuilders.donate.impl.Corpse -> stat.activeCorpse =
                                    Corpse.valueOf(pos.objectName)
                                is KillMessage -> stat.activeKillMessage = data.KillMessage.valueOf(pos.objectName)
                                is NameTag -> stat.activeNameTag = data.NameTag.valueOf(pos.objectName)
                                is StarterKit -> stat.activeKit = data.StarterKit.valueOf(pos.objectName)
                                is StepParticle -> stat.activeParticle = data.StepParticle.valueOf(pos.objectName)
                            }
                            Anime.title(player, "Выбрано!")
                            return@onClick
                        }
                        if (isDonate) {
                            buy(player, (pos.getPrice() * (100.0 - sale) / 100.0).toInt(), pos)
                            return@onClick
                        }
                        if (stat.money < pos.getPrice()) {
                            Anime.killboardMessage(player, Formatting.error("Недостаточно монет!"))
                            Glow.animate(player, 0.4, GlowColor.RED)
                            return@onClick
                        }
                        user.giveMoney(-pos.getPrice())
                        pos.give(user)
                        Glow.animate(player, 0.4, GlowColor.GREEN)
                    }
                    title = if (current) "[ Выбрано ]" else if (has) "§7Выбрать" else "§bКупить"
                    description = pos.getTitle()
                }, pos).apply { if (pos is MoneyKit) sale(pos.percent) }
            }.toMutableList()
        }.open(player)
    }

    private val all = selection {
        title = "BridgBuilders"
        rows = 5
        columns = 2
        hint = "Открыть"
        buttons(
            button {
                title = "Монеты"
                description = "§7Приобретите монеты, §7и ни в чем себе §7не отказывайте."
                item = dev.implario.bukkit.item.item {
                    type = Material.CLAY_BALL
                    enchant(Enchantment.LUCK, 0)
                    nbt("Монеты", 63)
                    nbt("other", "new_lvl_rare_close")
                }.build()
                onClick { player, _, _ ->
                    temp(
                        player,
                        "BridgeBuilders",
                        true,
                        *MoneyKit.values()
                    ) { button, money -> button.item(money.getIcon()) }
                }
            }, button {
                title = "Могилы"
                description = "§7Выберите могилу, которая §7появится на месте §7вашей смерти."
                item = dev.implario.bukkit.item.item {
                    type = Material.CLAY_BALL
                    nbt("other", "g2")
                    nbt("HideFlags", 63)
                }.build()
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Могилы",
                        false,
                        *me.reidj.bridgebuilders.donate.impl.Corpse.values()
                    ) { button, corpse -> button.item(corpse.getIcon()) }
                }
            }, button {
                title = "Частицы ходьбы"
                description = "§7Выберите тип частиц, §7которые будут появлять §7следом за вами."
                item = dev.implario.bukkit.item.item {
                    type = Material.CLAY_BALL
                    nbt("other", "guild_members_add")
                    nbt("HideFlags", 63)
                }.build()
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Частицы ходьбы",
                        false,
                        *StepParticle.values()
                    ) { button, step -> button.item(step.getIcon()) }
                }
            }, button {
                title = "Псевдонимы"
                description = "§7Выберите псевдоним, §7который появится в §7табе."
                item = dev.implario.bukkit.item.item {
                    type = Material.CLAY_BALL
                    nbt("other", "new_booster_2")
                    nbt("HideFlags", 63)
                }.build()
                onClick { player, _, _ ->
                    temp(player, "Псевдонимы", false, *NameTag.values()) { button, tag ->
                        button.item(tag.getIcon())
                    }
                }
            }, button {
                title = "Сообщения об убийстве"
                description = "§7Выберите сообщение, §7которое будет написано, когда §7вы убьете кого-то."
                item = dev.implario.bukkit.item.item {
                    type = Material.IRON_SPADE
                    nbt("simulators", "luck_shovel")
                    nbt("HideFlags", 63)
                }.build()
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Сообщения об убийстве",
                        false,
                        *KillMessage.values()
                    ) { button, message -> button.item(message.getIcon()) }
                }
            }, button {
                title = "Стартовые наборы"
                description = "§7Выберите набор, который §7поможет вам в игре."
                item = dev.implario.bukkit.item.item {
                    type = Material.CLAY_BALL
                    nbt("other", "bag")
                    nbt("HideFlags", 63)
                }.build()
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Стартовые наборы",
                        false,
                        *StarterKit.values()
                    ) { button, kit -> button.item(kit.getIcon()) }
                }
            }, button {
                title = "Стартовый набор"
                description = "Вы получите §b3 лутбокса §7и §e512 монет§7."
                item = dev.implario.bukkit.item.item {
                    type = Material.CLAY_BALL
                    nbt("other", "unique")
                    nbt("HideFlags", 63)
                }.build()
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Стартовый набор",
                        true,
                        *StarterPack.values()
                    ) { button, kit -> button.item(kit.getIcon()) }
                }
            }, button {
                title = "Достижения"
                description = ""
                item = dev.implario.bukkit.item.item {
                    type = Material.CLAY_BALL
                    nbt("other", "new_booster_1")
                    nbt("HideFlags", 63)
                }.build()
                onClick { player, _, _ ->
                    val user = app.getUser(player)!!
                    selection {
                        title = "Достижения"
                        money = ""
                        hint = ""
                        rows = 3
                        columns = 2
                        storage = Achievement.values().map { oldAchievement ->
                            val achievement = data.Achievement.valueOf(oldAchievement.name)
                            val playerHas = user.stat.achievement.contains(achievement)
                            val canGet = oldAchievement.predicate(user)
                            button {
                                item = item {
                                    type = Material.CLAY_BALL
                                    if (playerHas) nbt("other", "new_booster_0") else nbt(
                                        "other",
                                        "new_booster_1"
                                    )
                                }.build()
                                title =
                                    if (playerHas) "§aНаграда получена §7${oldAchievement.title}" else "§b${oldAchievement.title}"
                                hint(if (canGet && !playerHas) "Забрать награду!" else "")
                                description = oldAchievement.lore
                                onClick { player, _, _ ->
                                    if (!canGet || playerHas)
                                        return@onClick
                                    player.closeInventory()
                                    player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1f)
                                    oldAchievement.reward(user)
                                    user.stat.achievement.add(achievement)
                                    player.sendMessage(Formatting.fine("Вы успешно получили награду!"))
                                    clientSocket.write(SaveUserPackage(user.stat.uuid, user.stat))
                                }
                            }
                        }.toMutableList()
                    }.open(player)
                }
            }
        )
    }

    init {
        val npcLabel = worldMeta.getLabel("guide")
        // Создание подсветки NPC
        B.repeat(5) { ParticleHelper.happyVillager(npcLabel) }

        // Команда для открытия меню
        B.regCommand({ player, _ ->
            all.open(player)
            null
        }, "menu", "help")
    }

    private fun buy(player: Player, money: Int, donate: DonatePosition) {
        Confirmation("Купить §a${donate.getTitle()}\n§fза §b$money кристалик(а)") {
            ISocketClient.get().writeAndAwaitResponse<MoneyTransactionResponsePackage>(
                MoneyTransactionRequestPackage(player.uniqueId, money, true, donate.getTitle())
            ).thenAccept {
                val user = app.getUser(player)!!
                if (it.errorMessage != null) {
                    Anime.killboardMessage(player, Formatting.error(it.errorMessage))
                    Glow.animate(player, 0.4, GlowColor.RED)
                    return@thenAccept
                }
                Anime.title(player, Formatting.fine("Успешно!"))
                Anime.close(player)
                Glow.animate(player, 0.4, GlowColor.GREEN)
                donate.give(user)
                player.sendMessage(Formatting.fine("Спасибо за поддержку разработчиков!"))
                clientSocket.write(SaveUserPackage(player.uniqueId, user.stat))
            }
        }.open(player)
    }
}