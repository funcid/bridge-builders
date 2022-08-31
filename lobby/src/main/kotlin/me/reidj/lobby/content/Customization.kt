package me.reidj.lobby.content

import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.selection.Button
import me.func.mod.selection.Confirmation
import me.func.mod.selection.button
import me.func.mod.selection.selection
import me.func.mod.util.after
import me.func.mod.util.command
import me.func.protocol.GlowColor
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.data.AchievementType
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.getTexture
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import org.bukkit.Sound
import org.bukkit.entity.Player
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.GetAccountBalancePackage
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage
import java.util.concurrent.TimeUnit

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class Customization {

    private fun <T : DonatePosition> temp(
        player: Player,
        title: String,
        isDonate: Boolean,
        rows: Int,
        columns: Int,
        vararg donate: T,
        converter: (Button, T) -> Button = { button, _ -> button }
    ) {
        selection {
            val user = getUser(player) ?: return@selection
            val stat = user.stat

            this.title = title
            this.rows = rows
            this.columns = columns

            vault = if (isDonate) "donate" else "ruby"

            money = if (isDonate) {
                val balance = ISocketClient.get().writeAndAwaitResponse<GetAccountBalancePackage>(
                    GetAccountBalancePackage(player.uniqueId)
                ).get(1, TimeUnit.SECONDS).balanceData
                "Кристалликов ${balance.coins + balance.crystals}"
            } else {
                "Эфира " + stat.ether
            }
            storage = donate.map { pos ->
                converter(button {
                    val has = pos.getName() in stat.donates
                    val current = has && when (pos) {
                        is GraveType -> stat.currentGrave == pos.name
                        is MessageType -> stat.currentMessages == pos.name
                        is NameTagType -> stat.currentNameTag == pos.name
                        is StartingKit -> stat.currentStarterKit == pos.name
                        is WalkingEffectType -> stat.currentWalkingEffect == pos.name
                        else -> false
                    }
                    price = if (!isDonate) pos.getEther().toLong() else pos.getCrystals().toLong()
                    hint(if (current) "Выбрано" else if (has) "Выбрать" else "Купить")
                    onClick { player, _, _ ->
                        if (current)
                            return@onClick
                        Anime.close(player)
                        if (has) {
                            when (pos) {
                                is GraveType -> stat.currentGrave = pos.name
                                is MessageType -> stat.currentMessages = pos.name
                                is NameTagType -> stat.currentNameTag = pos.name
                                is StartingKit -> stat.currentStarterKit = pos.name
                                is WalkingEffectType -> stat.currentWalkingEffect = pos.name
                            }
                            Anime.title(player, "§dВыбрано!")
                            clientSocket.write(
                                SaveUserPackage(
                                    player.uniqueId,
                                    user.stat
                                )
                            )
                            return@onClick
                        }
                        buy(player, isDonate, pos)
                    }
                    this.title =
                        (if (current) "[ Выбрано ]" else if (has) "§7Выбрать" else "§bКупить") + " " + pos.getTitle()
                }, pos)
            }.toMutableList()
        }.open(player)
    }

    private val all = selection {
        title = "BridgeBuilders"
        rows = 3
        columns = 1
        hint = ""
    }

    private val subMenu = selection {
        title = "Псевдонимы"
        rows = 2
        columns = 2
        hint = "Открыть"
        buttons(
            button {
                title = "Платные псевдонимы"
                texture = getTexture("donate_tag")
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Донат псевдонимы",
                        true,
                        3,
                        2,
                        *NameTagType.values().filter { it.getCrystals() >= 0 }.toTypedArray()
                    ) { button, tag -> button.item(tag.getIcon()) }
                }
            },
            button {
                title = "Обычные псевдонимы"
                texture = getTexture("default_tag")
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Бесплатные псевдонимы",
                        false,
                        3,
                        2,
                        *NameTagType.values().filter { it.getEther() >= 0 }.toTypedArray()
                    ) { button, tag -> button.item(tag.getIcon()) }
                }
            })
    }

    private val donateSubMenu = selection {
        title = "Категории"
        rows = 2
        columns = 2
        hint = "Открыть"
        buttons(
            button {
                title = "Монеты"
                texture = getTexture("money_category")
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Монеты",
                        true,
                        3,
                        2,
                        *MoneyKitType.values()
                    ) { button, money ->
                        button.texture(money.getTexture()!!)
                        button.description(money.getDescription())
                    }
                }
            },
            button {
                title = "Наборы"
                texture = getTexture("kit_category")
                onClick { player, _, _ ->
                    temp(
                        player,
                        "Наборы",
                        true,
                        3,
                        2,
                        *LootBoxKit.values()
                    ) { button, kit ->
                        button.texture(kit.getTexture()!!)
                        button.hover(kit.getDescription())
                    }
                }
            }
        )
    }

    private val buttons = listOf(
        button {
            title = "Донат"
            description = "Поддержать разработчика"
            hint("Открыть")
            texture = getTexture("donate_shop")
            onClick { player, _, _ -> donateSubMenu.open(player) }
        },
        button {
            title = "Достижения"
            description = "Выполняйте задания и получайте за это приятные награды!"
            hint("Открыть")
            texture = getTexture("achievement_icon")
            onClick { player, _, _ ->
                val user = getUser(player) ?: return@onClick
                selection {
                    title = "Достижения"
                    rows = 3
                    columns = 2
                    hint = ""
                    storage = AchievementType.values().map { achievement ->
                        val playerHas = achievement.name in user.stat.achievements
                        val canGet = achievement.predicate(user)
                        button {
                            texture = getTexture("achievement_icon")
                            title =
                                if (playerHas) "§aНаграда получена §7${achievement.title}" else "§b${achievement.title}"
                            hint(if (canGet && !playerHas) "Забрать награду!" else "")
                            hover(achievement.lore)
                            onClick top@{ player, _, _ ->
                                if (!canGet || playerHas || user.isArmLock)
                                    return@top
                                user.isArmLock = true
                                Anime.close(player)
                                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1f)
                                achievement.reward(user)
                                user.stat.achievements.add(achievement.name)
                                player.sendMessage(Formatting.fine("Вы успешно получили награду!"))
                                clientSocket.write(
                                    SaveUserPackage(
                                        user.stat.uuid,
                                        user.stat
                                    )
                                )
                                after(5) { user.isArmLock = false }
                            }
                        }
                    }.toMutableList()
                }.open(player)
            }
        },
        button {
            title = "Стартовые наборы"
            hint("Открыть")
            description = "Выберите набор, который поможет вам в игре."
            texture = getTexture("starting_kit")
            onClick { player, _, _ ->
                temp(
                    player,
                    "Стартовые наборы",
                    false,
                    3,
                    1,
                    *StartingKit.values()
                ) { button, kit ->
                    button.texture(kit.getTexture())
                    if (kit.getLevel() > 0)
                        button.description("Доступно с §3${kit.getLevel()} §fуровня")
                    button.hover(kit.getDescription())
                }
            }
        },
        button {
            title = "Могилы"
            hint("Открыть")
            description = "Выберите могилу, которая появится на месте вашей смерти."
            texture = getTexture("grave")
            onClick { player, _, _ ->
                temp(
                    player,
                    "Могилы",
                    false,
                    3,
                    2,
                    *GraveType.values()
                ) { button, grave ->
                    button.item(grave.getIcon())
                    button.description(grave.getDescription())
                }
            }
        }, button {
            title = "Следы"
            hint("Открыть")
            description = "Выберите тип частиц, которые будут появляться следом за вами."
            texture = getTexture("walking")
            onClick { player, _, _ ->
                temp(
                    player,
                    "Частицы ходьбы",
                    false,
                    3,
                    2,
                    *WalkingEffectType.values()
                ) { button, walking ->
                    button.item(walking.getIcon())
                    button.description(walking.getDescription())
                }
            }
        }, button {
            title = "Псевдонимы"
            hint("Открыть")
            description = "Выберите псевдоним, который появится в табе."
            texture = getTexture("tag")
            onClick { player, _, _ -> subMenu.open(player) }
        }, button {
            title = "Сообщения об убийстве"
            hint("Открыть")
            description = "Выберите сообщение, которое будет написано, когда вы убьете кого-то."
            texture = getTexture("messages")
            onClick { player, _, _ ->
                temp(
                    player,
                    "Сообщения об убийстве",
                    false,
                    2,
                    2,
                    *MessageType.values()
                ) { button, message ->
                    button.item(message.getIcon())
                    button.description(message.getDescription())
                }
            }
        }, button {
            title = "Ресурспак"
            hint("Переключить")
            texture = getTexture("resourcepack")
            onClick { player, _, button ->
                val user = getUser(player) ?: return@onClick
                val stat = user.stat
                stat.isApprovedResourcepack = !stat.isApprovedResourcepack
                button.hint(if (stat.isApprovedResourcepack) "Выключить автоматическую установку" else "Включить автоматическую установку")
            }
        }
    )

    init {
        command("menu") { player, _ ->
            val user = getUser(player) ?: return@command
            val stat = user.stat
            all.apply {
                storage.clear()
                storage.add(button {
                    title = "Общая статистика"
                    description = "Наведите, чтобы посмотреть"
                    texture = getTexture("information")
                    hover(
                        """
                        Уровень: §3${user.getLevel()}
                        Опыта: §3${stat.experience}
                        Побед: §3${stat.wins}
                        Эфира: §3${stat.ether}
                        Убийств: §3${stat.kills}
                        Сыграно: §3${stat.games} §fигры(ы)
                        Лутбоксов: §3${stat.lootBoxes.size}
                        Лутбоксов открыто: §3${stat.lootBoxOpened}
                        Достижений получено: §3${stat.achievements.size}§f/§3${AchievementType.values().size}
                        Могил получено: §3${stat.graves.size}§f/§3${GraveType.values().size}
                        Сообщений получено: §3${stat.messages.size}§f/§3${MessageType.values().size}
                        Префиксов получено: §3${stat.nameTags.size}§f/§3${NameTagType.values().size}
                        Стартовых наборов получено: §3${stat.startingKits.size}§f/§3${StartingKit.values().size}
                        Следов получено: §3${stat.walkingEffects.size}§f/§3${WalkingEffectType.values().size}
                    """.trimIndent()
                    )
                })
                storage.addAll(buttons)
            }.open(player)
        }
    }

    private fun buy(player: Player, isDonate: Boolean, donate: DonatePosition) {
        Confirmation(
            "Купить §a${donate.getTitle()}",
            "за ${if (isDonate) donate.getCrystals() else donate.getEther()} ${if (isDonate) "§bКристаллик(а)" else "§dЭфира"}"
        ) {
            val user = getUser(player) ?: return@Confirmation
            val stat = user.stat
            if (isDonate) {
                clientSocket.writeAndAwaitResponse<MoneyTransactionResponsePackage>(
                    MoneyTransactionRequestPackage(
                        player.uniqueId,
                        donate.getCrystals(),
                        true,
                        donate.getTitle()
                    )
                ).thenAccept {
                    if (it.errorMessage != null) {
                        Anime.killboardMessage(player, Formatting.error(it.errorMessage))
                        Glow.animate(player, 0.4, GlowColor.RED)
                        return@thenAccept
                    }
                    Anime.title(player, "§dУспешно!")
                    Anime.close(player)
                    Glow.animate(player, 0.4, GlowColor.GREEN)
                    donate.give(user)
                    stat.donates.add(donate.getName())
                    player.sendMessage(Formatting.fine("Спасибо за поддержку разработчиков!"))
                    clientSocket.write(SaveUserPackage(player.uniqueId, stat))
                }
            } else {
                if (stat.ether < donate.getEther()) {
                    Anime.killboardMessage(player, Formatting.error("Недостаточно Эфира!"))
                    Glow.animate(player, 0.4, GlowColor.RED)
                    return@Confirmation
                } else if (user.getLevel() < donate.getLevel()) {
                    Anime.killboardMessage(player, Formatting.error("Ваш уровень ниже того, что требуется!"))
                    Glow.animate(player, 0.4, GlowColor.RED)
                    return@Confirmation
                }
                user.giveEther(-donate.getEther())
                donate.give(user)
                Glow.animate(player, 0.4, GlowColor.GREEN)
                stat.donates.add(donate.getName())
                Anime.title(player, Formatting.fine("§dУспешно!"))
                clientSocket.write(SaveUserPackage(player.uniqueId, stat))
            }
        }.open(player)
    }
}