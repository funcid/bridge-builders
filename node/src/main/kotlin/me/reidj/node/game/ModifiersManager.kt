package me.reidj.node.game

import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.selection.Button
import me.func.mod.selection.choicer
import me.reidj.bridgebuilders.error
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.cristalix.core.formatting.Formatting
import java.util.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object ModifiersManager {

    val modifiersItem = item {
        type = Material.PAPER
        text("§bМодификаторы игры")
        nbt("click", "modifiers")
    }

    private val choicer = choicer {
        title = "BridgeBuilders"
        description = "Модификаторы игры"
    }

    fun voteRemove(player: Player) {
        val uuid = player.uniqueId
        ModifiersType.values().toMutableSet().removeIf {
            it.voted.remove(uuid)
            uuid in it.voted
        }
    }

    fun modifierAccept(player: Player) =
        ModifiersType.values().map { it to it.voted.size }.sortedBy { -it.second }.subList(0, 1)
            .first().first.on(player)

    fun open(player: Player) {
        choicer.storage = ModifiersType.values().map { type ->
            Button()
                .title(type.title)
                .description("Голосов: §3${type.voted.size}")
                .hover(type.description)
                .texture("minecraft:mcpatcher/cit/bridgebuilders/${type.texture}.png")
                .special(false)
                .hint("Голосовать")
                .onLeftClick { player, _, _ ->
                    val uuid = player.uniqueId
                    if (ModifiersType.values().map { it.voted }.any { it.any { voted -> voted == uuid } } || uuid in type.voted) {
                        player.error("Ошибка", "Ваш голос уже был отдан!")
                        Anime.close(player)
                        return@onLeftClick
                    }
                    type.voted.add(uuid)
                    Anime.close(player)
                    player.sendMessage(Formatting.fine("Вы проголосовали за модификатор §b${type.title}"))
                }
                .onRightClick { player, _, _ ->
                    val uuid = player.uniqueId
                    if (uuid in type.voted) {
                        type.voted.remove(uuid)
                        Anime.close(player)
                        player.sendMessage("Вы забрали свой голос за модификатор §b${type.title}")
                    } else {
                        player.error("Ошибка", "Вы не голосовали за этот модификатор!")
                        Anime.close(player)
                    }
                }
        }.toMutableList()
        choicer.open(player)
    }

    enum class ModifiersType(
        val title: String,
        val description: String,
        val texture: String,
        val on: Player.() -> Unit,
        val voted: MutableSet<UUID> = mutableSetOf()
    ) {
        BOER(
            "Бур",
            "На старте игры всем командам выдаётся \nУскоренное копание на 8 минут. \nПо истечению времени - оно пропадает.",
            "uskor",
            { addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 480 * 20, 0)) }
        ),
        IMPLANT("Имплант", "Всем игрокам вживляется Имплант, \nкоторый даёт двойное здоровье на всю игру",
            "implant",
            {
                maxHealth = 40.0
                health = 40.0
            }),
        DEFAULT("Обычная игра", "Стандартная игра без дополнений", "default", {}),
        ;
    }
}