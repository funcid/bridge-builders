package me.reidj.node.game

import me.reidj.node.activeStatus
import me.reidj.node.timer.Status
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class UnusedListeners : Listener {

    @EventHandler
    fun InventoryClickEvent.handle() {
        isCancelled = activeStatus == Status.STARTING
    }

    @EventHandler
    fun FoodLevelChangeEvent.handle() {
        if (activeStatus == Status.STARTING) level = 20
    }

    @EventHandler
    fun PlayerDropItemEvent.handle() {
        isCancelled = activeStatus == Status.STARTING
    }

    @EventHandler
    fun CraftItemEvent.handle() {
        if (inventory.result == null)
            return
        if (inventory.result.getType() == Material.FLINT_AND_STEEL) {
            inventory.result = null
        }
    }
}