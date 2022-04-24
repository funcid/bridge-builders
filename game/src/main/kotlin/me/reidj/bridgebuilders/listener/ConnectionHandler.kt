package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.Npc
import me.func.mod.conversation.ModLoader
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
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.item.Items
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus
import java.util.*

object ConnectionHandler : Listener {

    private val back = item {
        type = Material.CLAY_BALL
        nbt("other", "cancel")
        text("§cВернуться")
    }.build()

    val markers = mutableListOf<Marker>()

    @EventHandler
    fun PlayerJoinEvent.handle() = player.apply {
        inventory.clear()

        val user = app.getUser(uniqueId)

        if (user == null) {
            sendMessage(Formatting.error("Нам не удалось прогрузить Вашу статистику."))
            B.postpone(3) {
                Cristalix.transfer(
                    setOf(player.uniqueId),
                    RealmId.of("BRIL-1")
                )
            }
        }


        if (user?.player == null)
            user?.player = player

        ModLoader.send("bridge-mod-bundle.jar", this)

        B.postpone(5) { teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5)) }

        if (activeStatus == Status.STARTING) {
            gameMode = GameMode.ADVENTURE
            inventory.setItem(8, back)
            teams.forEach {
                inventory.addItem(
                    Items.builder()
                        .displayName("Выбрать команду: " + it.color.chatFormat + it.color.teamName)
                        .type(Material.WOOL)
                        .color(it.color)
                        .build()
                )
            }
            B.postpone(5) {
                // Создание маркера
                teams.forEach {
                    markers.add(
                        Anime.marker(
                            this,
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
                            this,
                            marker.uuid,
                            marker.x,
                            marker.y - if (up) 0.0 else 0.7,
                            marker.z,
                            0.75
                        )
                    }
                }
                Npc.npcs.values.forEach { it.spawn(this) }
            }
        } else {
            gameMode = GameMode.SPECTATOR
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        if (app.isSpectator(player))
            return
        teams.forEach { it.players.remove(player.uniqueId) }
        app.updateNumbersPlayersInTeam()
    }

    @EventHandler
    fun AsyncPlayerPreLoginEvent.handle() {
        if (activeStatus != Status.STARTING) {
            playerProfile.properties.forEach { profileProperty ->
                if (profileProperty.value == "PARTY_WARP") {
                    if (IRealmService.get().currentRealmInfo.status != RealmStatus.WAITING_FOR_PLAYERS) {
                        disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Сейчас нельзя зайти на этот сервер")
                        loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
                        playerDataManager.userMap.remove(uniqueId)
                    }
                }
            }
        }
    }
}