package me.reidj.lobby.content

import me.func.mod.battlepass.BattlePass
import me.func.mod.battlepass.BattlePassPageAdvanced

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class BattlePass {

    companion object {
        val battlePass = BattlePass.new(299) {
            pages = arrayListOf(
                BattlePassPageAdvanced(
                    300,
                    10,
                    listOf()
                )
            )
        }
    }
}