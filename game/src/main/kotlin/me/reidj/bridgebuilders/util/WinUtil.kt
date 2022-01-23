package me.reidj.bridgebuilders.util

import me.reidj.bridgebuilders.*

object WinUtil {

    fun check4win(): Boolean {
        if (activeStatus != Status.GAME)
            return false
        if (teams.all { it.players.size == 0 })
            return true
        // Если время вышло игроки победили
        if (activeStatus.lastSecond * 20 == timer.time) {
            winMessage = "§aВремя вышло! Победила дружба."
            return true
        }
        return false
    }
}