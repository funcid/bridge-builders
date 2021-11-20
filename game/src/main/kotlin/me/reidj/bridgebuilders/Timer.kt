package me.reidj.bridgebuilders

import clepto.bukkit.B
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable

lateinit var timer: Timer

val toDelete: MutableList<Location> = mutableListOf()

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        if (time % 20 == 0) {
            teams.forEach {
                it.breakBlocks.forEach { block ->
                    when (block.value) {
                        Material.IRON_ORE -> B.postpone(20 * 5) {
                            block.key.block.type = Material.IRON_ORE
                            toDelete.add(block.key)
                        }
                        Material.DIAMOND_ORE -> B.postpone(20 * 6) {
                            block.key.block.type = Material.DIAMOND_ORE
                            toDelete.add(block.key)
                        }
                        Material.COAL_ORE -> B.postpone(20 * 3) {
                            block.key.block.type = Material.COAL_ORE
                            toDelete.add(block.key)
                        }
                        Material.GOLD_ORE -> B.postpone(20 * 4) {
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