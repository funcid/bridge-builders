package me.reidj.bridgebuilders.user

import implario.humanize.Humanize
import me.func.mod.Anime
import me.reidj.bridgebuilders.team.Team
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import user.Stat

class User(stat: Stat) {

    var collectedBlocks = 0
    var kills = 0
    var exp = 0F
    var inGame = false

    var stat: Stat
    var player: Player? = null
    var lastDamager: Player? = null
    var team: Team? = null
    var isArmLock = false
    var isGod = false

    var inventory: Inventory? = null

    init {
        this.stat = stat
    }

    fun giveMoney(money: Int, isLobby: Boolean) = changeMoney(money, isLobby)

    fun minusMoney(money: Int) = changeMoney(-money, true)

    private fun changeMoney(dMoney: Int, isLobby: Boolean) {
        stat.money += dMoney
        if (isLobby)
            Anime.bottomRightMessage(player!!, "§e${stat.money} ${Humanize.plurals("монета", "монеты", "монет", stat.money)}")
    }
}