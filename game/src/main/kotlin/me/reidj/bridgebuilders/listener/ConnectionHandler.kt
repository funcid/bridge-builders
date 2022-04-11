package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.Npc
import me.func.protocol.Marker
import me.func.protocol.MarkerSign
import me.reidj.bridgebuilders.*
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
import java.util.*

object ConnectionHandler : Listener {

    private val back = item {
        type = Material.CLAY_BALL
        nbt("other", "cancel")
        text("§cВернуться")
    }

    private val markers = mutableListOf<Marker>()

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.inventory.clear()

        if (activeStatus == Status.STARTING)
            player.gameMode = GameMode.ADVENTURE

        B.postpone(5) {
            // Создание маркера
            teams.forEach {
                markers.add(
                    Anime.marker(
                        player,
                        Marker(
                            UUID.randomUUID(),
                            it.teleport.x + 0.5,
                            it.teleport.y + 1.5,
                            it.teleport.z + 0.5,
                            16.0,
                            MarkerSign.ARROW_DOWN.texture
                        )
                    )
                )
            }
            // Движение маркера
            markers.forEach { marker ->
                var up = false
                B.repeat(15) {
                    up = !up
                    Anime.moveMarker(
                        player,
                        marker.uuid,
                        marker.x,
                        marker.y - if (up) 0.0 else 0.7,
                        marker.z,
                        0.75
                    )
                }
            }
            Npc.npcs.values.forEach { it.spawn(player)}
        }

        if (activeStatus != Status.STARTING) {
            player.gameMode = GameMode.SPECTATOR
            return
        }

        getByPlayer(player).stat.lastEnter = System.currentTimeMillis()

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
        if (app.isSpectator(player))
            return
        val user = getByPlayer(player)
        teams.forEach { it.players.remove(player.uniqueId) }
        user.stat.timePlayedTotal += System.currentTimeMillis() - user.stat.lastEnter
    }

    @EventHandler
    fun AsyncPlayerPreLoginEvent.handle() {
        if (activeStatus != Status.STARTING) {
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
}