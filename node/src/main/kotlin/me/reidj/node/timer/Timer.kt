package me.reidj.node.timer

import me.reidj.node.activeStatus
import me.reidj.node.game.BridgeGame
import me.reidj.node.teams

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

class Timer(private val game: BridgeGame) {
    var time = 0

    fun tick() = run {
        if (time % 3 == 0)
            teams.forEach { it.bridge.placeBlock(it) }
        time = activeStatus.now(time, game) + 1
    }
}