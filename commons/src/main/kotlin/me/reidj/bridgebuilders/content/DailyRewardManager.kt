package me.reidj.bridgebuilders.content

import me.reidj.bridgebuilders.mod.ModTransfer
import me.reidj.bridgebuilders.user.User

object DailyRewardManager {

    fun open(user: User) {
        val transfer = ModTransfer().integer(user.stat.rewardStreak + 1)
        WeekRewards.values().forEach { transfer.item(it.icon).string("§7Награда: " + it.title) }
        transfer.send("bridge:weekly-reward", user)
    }

}