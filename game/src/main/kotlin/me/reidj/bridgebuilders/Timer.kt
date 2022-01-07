package me.reidj.bridgebuilders

import clepto.bukkit.B
import me.reidj.bridgebuilders.donate.impl.StepParticle
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable

lateinit var timer: Timer

val toDelete: MutableList<Location> = mutableListOf()

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        if (time % 2 == 0) {
            Bukkit.getOnlinePlayers().filter { it.gameMode != GameMode.SPECTATOR }
                .forEach {
                    val particle = getByPlayer(it).stat.activeParticle
                    if (particle != StepParticle.NONE)
                        it.world.spawnParticle(particle.type, it.location.clone().add(0.0, 0.2, 0.0), 1)
                }
        }
        if (time % 10 == 0) {
            teams.forEach { app.addBlock(it) }
        }
        if (time % 20 == 0 && activeStatus == Status.GAME) {
            teams.forEach {
                it.breakBlocks.forEach { block ->
                    when (block.value) {
                        Material.IRON_ORE -> B.postpone(20 * 50) {
                            block.key.block.type = Material.IRON_ORE
                            toDelete.add(block.key)
                        }
                        Material.DIAMOND_ORE -> B.postpone(20 * 60) {
                            block.key.block.type = Material.DIAMOND_ORE
                            toDelete.add(block.key)
                        }
                        Material.COAL_ORE -> B.postpone(20 * 30) {
                            block.key.block.type = Material.COAL_ORE
                            toDelete.add(block.key)
                        }
                        Material.GOLD_ORE -> B.postpone(20 * 40) {
                            block.key.block.type = Material.GOLD_ORE
                            toDelete.add(block.key)
                        }
                        else -> toDelete.add(block.key)
                    }
                }
            }
            toDelete.forEach { teams.forEach { team -> team.breakBlocks.remove(it) } }
        }
        time = activeStatus.now(time) + 1
    }
}