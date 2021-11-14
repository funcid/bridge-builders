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
        if (activeStatus != Status.GAME)
            cancelled = true
    }

    @EventHandler
    fun PlayerDeathEvent.handle() {
        cancelled = true

        val player = entity as Player

        B.bc("§a ${player.name} §fбыл убит игроком ${player.killer.name}")

        if (player.gameMode == GameMode.SPECTATOR)
            return

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

        player.inventory.contents.toList().stream()
            .filter { it != null }
            .forEach {
                when (it.amount > 4) {
                    true -> it.amount -= 2
                    false -> toDelete.add(it)
                }
            }
        toDelete.forEach { player.inventory.remove(it) }
    }
}