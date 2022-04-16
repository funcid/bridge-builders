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
                .forEach { user ->
                    val particle = user.stat.activeParticle
                    if (particle != data.StepParticle.NONE && user.player!!.world != null) {
                        try {
                            user.player!!.world.spawnParticle(
                                StepParticle.valueOf(particle.name).type,
                                user.player!!.location.clone().add(0.0, 0.2, 0.0),
                                1
                            )
                        } catch (ex: NullPointerException) {
                            ex.printStackTrace()
                            println(user)
                            println(user.player)
                            println(user.player?.world)
                            println(particle)
                            println(StepParticle.valueOf(particle.name))
                            println(StepParticle.valueOf(particle.name).type)
                        }
                    }
                }
        }
        if (time % 5 == 0)
            teams.forEach { app.addBlock(it) }
        time = activeStatus.now(time) + 1
    }
}