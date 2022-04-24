package me.reidj.bridgebuilders

import me.reidj.bridgebuilders.donate.impl.StepParticle
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable

lateinit var timer: Timer

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        if (time % 3 == 0) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.gameMode == GameMode.SPECTATOR)
                    continue
                val user = app.getUser(player)!!
                val particle = user.stat.activeParticle
                if (particle != data.StepParticle.NONE && player.world != null) {
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
        if (time % 5 == 0)
            teams.forEach { app.addBlock(it) }
        time = activeStatus.now(time) + 1
    }
}