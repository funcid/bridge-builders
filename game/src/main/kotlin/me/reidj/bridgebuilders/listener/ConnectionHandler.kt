package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.reidj.bridgebuilders.Status
import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.teams
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import ru.cristalix.core.item.Items

object ConnectionHandler : Listener {

    private val back = item {
        type = Material.CLAY_BALL
        nbt("other", "cancel")
        text("§cВернуться")
    }.build()

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE

        B.postpone(5) {Anime.sendStandardMods(player)}

        if (activeStatus == Status.STARTING) {
            player.inventory.setItem(8, back)
            teams.forEach {
                player.inventory.addItem(
                    Items.builder()
                        .displayName("Выбрать команду: " + it.color.chatFormat + it.color.teamName)
                        .type(Material.WOOL)
                        .color(it.color)
                        .build()
                )
            }
        }
    }
}