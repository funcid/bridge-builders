package me.reidj.bridgebuilders.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.player.*
import packages.ChatPackage
import ru.cristalix.core.network.ISocketClient
import java.util.*

object GlobalListeners : Listener {

    @EventHandler
    fun BlockRedstoneEvent.handle() = apply { newCurrent = oldCurrent }

    @EventHandler
    fun PlayerInteractEntityEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockFadeEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockSpreadEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun EntityChangeBlockEvent.handle() = apply {
        isCancelled = true
        block.state.update(false, false)
    }

    @EventHandler
    fun BlockGrowEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockPhysicsEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun PlayerArmorStandManipulateEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun PlayerSwapHandItemsEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun AsyncPlayerChatEvent.handle() = ISocketClient.get().write(ChatPackage(player.name, message, Date()))

    @EventHandler
    fun PlayerCommandPreprocessEvent.handle() = ISocketClient.get().write(ChatPackage(player.name, message, Date()))
}