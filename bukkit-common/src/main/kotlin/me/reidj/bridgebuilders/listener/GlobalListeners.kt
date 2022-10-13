package me.reidj.bridgebuilders.listener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.data.Stat
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.protocol.LoadStatPackage
import me.reidj.bridgebuilders.user.User
import org.bukkit.Particle
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.player.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class GlobalListeners : Listener {

    @EventHandler
    fun AsyncPlayerPreLoginEvent.handle() = registerIntent(plugin).apply {
        CoroutineScope(Dispatchers.IO).launch {
            val statPackage = clientSocket.writeAndAwaitResponse<LoadStatPackage>(LoadStatPackage(uniqueId)).await()
            var stat = statPackage.stat
            if (stat == null)
                stat = Stat(
                    uniqueId,
                    "",
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    mutableSetOf(
                        WalkingEffectType.NONE.name,
                        GraveType.NONE.name,
                        MessageType.NONE.name,
                        NameTagType.NONE.name,
                        StartingKit.NONE.name
                    ),
                    mutableSetOf(WalkingEffectType.NONE.name),
                    mutableSetOf(GraveType.NONE.name),
                    mutableSetOf(MessageType.NONE.name),
                    mutableSetOf(NameTagType.NONE.name),
                    mutableSetOf(StartingKit.NONE.name),
                    mutableSetOf(),
                    mutableListOf(),
                    WalkingEffectType.NONE.name,
                    GraveType.NONE.name,
                    MessageType.NONE.name,
                    NameTagType.NONE.name,
                    StartingKit.NONE.name,
                    1.0,
                    -1L,
                    -1L,
                    isApprovedResourcepack = true,
                )
            userMap.putIfAbsent(uniqueId, User(stat))
            completeIntent(plugin)
        }
    }

    @EventHandler
    fun BlockRedstoneEvent.handle() {
        newCurrent = oldCurrent
    }

    @EventHandler
    fun PlayerInteractEntityEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockFadeEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockSpreadEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun EntityChangeBlockEvent.handle() {
        isCancelled = true
        block.state.update(false, false)
    }

    @EventHandler
    fun BlockGrowEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockPhysicsEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerArmorStandManipulateEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockFromToEvent.handle() {
        val id = block.typeId
        isCancelled = id == 8 || id == 9
    }

    @EventHandler
    fun ExplosionPrimeEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (player.isSpectator() || player.world != worldMeta.world)
            return
        val user = getUser(player) ?: return
        val particle = user.stat.currentWalkingEffect
        if (particle != WalkingEffectType.NONE.name) {
            val location = player.location
            player.world.spawnParticle(
                Particle.valueOf(particle),
                location.x,
                location.y + 0.2,
                location.z,
                1
            )
        }
    }
}
