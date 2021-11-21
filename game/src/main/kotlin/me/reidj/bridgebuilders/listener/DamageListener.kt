package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.bukkit.Cycle
import me.reidj.bridgebuilders.Status
import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.mod.ModHelper
import me.reidj.bridgebuilders.teams
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack

object DamageListener : Listener {

    private var toDelete: MutableList<ItemStack> = mutableListOf()

    @EventHandler
    fun EntityDamageEvent.handle() {
        if (activeStatus != Status.GAME || (entity as Player).gameMode == GameMode.SPECTATOR)
            cancelled = true
    }

    @EventHandler
    fun PlayerDeathEvent.handle() {
        cancelled = true

        val player = entity as Player

        if (player.killer != null)
            ModHelper.allNotification("§a ${player.name} §fбыл убит игроком ${player.killer.name}")

        if (player.gameMode == GameMode.SPECTATOR)
            return

        player.inventory
            .filterNotNull()
            .forEach {
                if (it.getAmount() > 4) {
                    it.setAmount(it.getAmount() - 2)
                } else {
                    val name = it.getType().name
                    if (name.endsWith("SWORD") || name.endsWith("AXE") || name.endsWith("PICKAXE")) {
                        if (it.getDurability() >= 0)
                            it.setDurability((it.getDurability() + 20).toShort())
                        else
                            toDelete.add(it)
                    } else {
                        toDelete.add(it)
                    }
                }
            }
        toDelete.forEach { player.inventory.remove(it) }
        toDelete.clear()

        player.gameMode = GameMode.SPECTATOR

        Cycle.run(20, 5) {
            if (it == 5) {
                player.gameMode = GameMode.SURVIVAL
                teams.stream()
                    .filter { team -> team.players.contains(player.uniqueId) }
                    .forEach { team -> player.teleport(team.location) }
                Cycle.exit()
            }
            ModHelper.sendTitle(app.getUser(player), "До возрождения ${5 - it}")
        }
    }
}