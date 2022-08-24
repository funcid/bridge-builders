package me.reidj.lobby.util

import dev.implario.bukkit.item.item
import org.bukkit.Material

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object ItemUtil {

    var gameItem = item {
        type = Material.CLAY_BALL
        text("§aИграть")
        nbt("other", "guild_members")
        nbt("click", "game")
    }
    var cosmeticItem = item {
        type = Material.CLAY_BALL
        text("§aПерсонаж")
        nbt("other", "clothes")
        nbt("click", "menu")
    }
    var backItem = item {
        type = Material.CLAY_BALL
        text("§cВыйти")
        nbt("other", "cancel")
        nbt("click", "leave")
    }
}