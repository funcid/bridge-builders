package me.reidj.bridgebuilders.listener

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.Status
import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.teams
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.cristalix.core.item.Items
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus

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
        val user = getByPlayer(player)

        user.stat.lastEnter = System.currentTimeMillis()

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

    @EventHandler
    fun PlayerQuitEvent.handle() {
        val user = getByPlayer(player)
        user.stat.timePlayedTotal += System.currentTimeMillis() - user.stat.lastEnter
    }

    @EventHandler
    fun AsyncPlayerPreLoginEvent.handle() {
        playerProfile.properties.forEach { profileProperty ->
            if (profileProperty.value == "PARTY_WARP") {
                if (IRealmService.get().currentRealmInfo.status != RealmStatus.WAITING_FOR_PLAYERS) {
                    disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Сейчас нельзя зайти на этот сервер")
                    loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
                }
            }
        }
    }
}