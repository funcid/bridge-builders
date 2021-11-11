package me.reidj.bridgebuilders

import org.bukkit.scheduler.BukkitRunnable

lateinit var timer: Timer

class Timer : BukkitRunnable() {
    var time = 0

    override fun run() {
        /*if (time % 2 == 0) {
        }*/
        time = activeStatus.now(time) + 1
    }
}