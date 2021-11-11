package me.reidj.bridgebuilders.util

import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.timer
import me.reidj.bridgebuilders.winMessage

object WinUtil {

    fun check4win(): Boolean {
        // Если время вышло игроки победили
        if (activeStatus.lastSecond * 20 == timer.time) {
            winMessage = "§aВремя вышло! Победила дружба."
            return true
        }
        return false
    }
}