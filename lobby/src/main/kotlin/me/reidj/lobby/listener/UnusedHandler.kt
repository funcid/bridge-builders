package me.reidj.lobby.listener

import me.reidj.bridgebuilders.createDisplayName
import me.reidj.bridgebuilders.getUser
import me.reidj.lobby.util.GameUtil.spawn
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.LeavesDecayEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerPickupItemEvent
import ru.cristalix.core.formatting.Formatting

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class UnusedHandler : Listener {

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (player.location.block.y <= 2)
            player.teleport(spawn)
    }

    @EventHandler
    fun LeavesDecayEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun EntityDamageEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockPhysicsEvent.handle() = apply { cancel = true }

    @EventHandler
    fun FoodLevelChangeEvent.handle() = apply { level = 20 }

    @EventHandler
    fun BlockBreakEvent.handle() = apply { cancel = true }

    @EventHandler
    fun BlockPlaceEvent.handle() = apply { cancel = true }

    @EventHandler
    fun PlayerPickupItemEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerDropItemEvent.handle() = apply { cancel = true }

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        val user = getUser(player) ?: return
        isCancelled = true
        Bukkit.getOnlinePlayers()
            .forEach { it.sendMessage("${createDisplayName(user)} ${Formatting.ARROW_SYMBOL} $message") }
    }
}