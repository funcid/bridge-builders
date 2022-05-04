package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.donate.impl.StepParticle
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.isSpectator
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.player.*
import packages.ChatPackage
import ru.cristalix.core.network.ISocketClient
import java.text.SimpleDateFormat
import java.util.*

private val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

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
    fun AsyncPlayerChatEvent.handle() =
        ISocketClient.get().write(ChatPackage(player.name, message, formatter.format(Date())))

    @EventHandler
    fun PlayerCommandPreprocessEvent.handle() =
        ISocketClient.get().write(ChatPackage(player.name, message, formatter.format(Date())))

    @EventHandler
    fun ExplosionPrimeEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (isSpectator(player))
            return
        val particle = getByPlayer.invoke(player)!!.stat.activeParticle
        if (particle != data.StepParticle.NONE) {
            val location = player.location
            player.world.spawnParticle(
                StepParticle.valueOf(particle.name).type,
                location.x,
                location.y + 0.2,
                location.z,
                1
            )
        }
    }
}