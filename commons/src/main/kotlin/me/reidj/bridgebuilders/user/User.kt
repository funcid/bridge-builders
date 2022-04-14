package me.reidj.bridgebuilders.user

import org.bukkit.entity.Player
import user.Stat

class User(stat: Stat) {

    var collectedBlocks = 0
    var kills = 0
    var activeHand = false

    var stat: Stat
    var player: Player? = null

    init {
        this.stat = stat
    }

    fun giveMoney(money: Int) = changeMoney(money)

    fun minusMoney(money: Int) = changeMoney(-money)

    private fun changeMoney(dMoney: Int) {
        stat.money += dMoney
        me.func.mod.conversation.ModTransfer(stat.money).send("bridge:balance", player)
    }
}