package me.reidj.bridgebuilders.util

import me.reidj.bridgebuilders.Status
import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.teams
import me.reidj.bridgebuilders.timer

object WinUtil {

    fun check4win(): Boolean {
        if (activeStatus != Status.GAME)
            return false
        if (teams.all { it.players.size == 0 }) {
            activeStatus = Status.END
            return true
        }
        // Если время вышло
        if (activeStatus.lastSecond * 20 == timer.time)
            return true
        return false
    }
}