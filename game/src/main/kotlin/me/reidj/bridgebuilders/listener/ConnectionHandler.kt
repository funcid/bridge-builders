package me.reidj.bridgebuilders.listener

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.donate.impl.NameTag
import net.md_5.bungee.api.chat.ComponentBuilder
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
import ru.cristalix.core.tab.IConstantTabView
import ru.cristalix.core.tab.ITabService
import ru.cristalix.core.tab.TabTextComponent
import ru.cristalix.core.text.TextFormat
import java.util.concurrent.CompletableFuture

object ConnectionHandler : Listener {

    var tab: ITabService = ITabService.get()
    val tabView: IConstantTabView = tab.createConstantTabView()

    init {
        // Таб
        tabView.addPrefix(
            TabTextComponent(
                1,
                TextFormat.RBRACKETS,
                { getByUuid(it).stat.activeNameTag != NameTag.NONE },
                { uuid ->
                    val tag = getByUuid(uuid).stat.activeNameTag
                    CompletableFuture.completedFuture(
                        ComponentBuilder(
                        if (tag != NameTag.NONE) tag.getRare().with(tag.getTitle()) else "").create())
                },
                { uuid -> CompletableFuture.completedFuture(Rare.values().size + 1 - getByUuid(uuid).stat.activeNameTag.getRare().ordinal) },
            )
        )
        tab.enable()
    }

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

        player.scoreboard.teams.forEach { it.unregister() }
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