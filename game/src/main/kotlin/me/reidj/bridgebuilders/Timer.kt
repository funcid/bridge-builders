package me.reidj.bridgebuilders

import me.reidj.bridgebuilders.donate.impl.StepParticle
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable

lateinit var timer: Timer

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        if (time % 2 == 0) {
            Bukkit.getOnlinePlayers().filter { it.gameMode != GameMode.SPECTATOR }
                .mapNotNull { app.getUser(it) }
                .forEach {
                    val particle = it.stat.activeParticle
                    if (particle != StepParticle.NONE)
                        it.player!!.world.spawnParticle(particle.type, it.player!!.location.clone().add(0.0, 0.2, 0.0), 1)
                }
        }
        if (time % 5 == 0)
            teams.forEach { app.addBlock(it) }
        time = activeStatus.now(time) + 1
    }
}