package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.donate.impl.StepParticle
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.isSpectator
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import java.text.SimpleDateFormat

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
    fun BlockFromToEvent.handle() {
        val id = block.typeId
        isCancelled = id == 8 || id == 9
    }

    @EventHandler
    fun ExplosionPrimeEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (isSpectator(player))
            return
        val particle = getByPlayer(player)?.stat?.activeParticle
        if (particle != data.StepParticle.NONE && player.world != null && player != null && player.location != null) {
            val location = player.location
            player.world.spawnParticle(
                particle?.let { StepParticle.valueOf(it.name).type },
                location.x,
                location.y + 0.2,
                location.z,
                1
            )
        }
    }
}