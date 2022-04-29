package me.reidj.bridgebuilders

import org.bukkit.scheduler.BukkitRunnable

lateinit var timer: Timer

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        if (time % 3 == 0)
            teams.forEach { app.addBlock(it) }
        time = activeStatus.now(time) + 1
    }
}