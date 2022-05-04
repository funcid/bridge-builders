package me.reidj.bridgebuilders.user

import me.reidj.bridgebuilders.team.Team
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import user.Stat

class User(stat: Stat) {

    var collectedBlocks = 0
    var kills = 0
    var activeHand = false
    var inGame = false

    var stat: Stat
    var player: Player? = null
    var lastDamager: Player? = null
    var team: Team? = null

    var inventory: Inventory? = null

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