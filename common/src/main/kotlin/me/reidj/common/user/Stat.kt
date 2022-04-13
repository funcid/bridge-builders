package me.reidj.common.user

import me.reidj.common.data.Unique
import java.util.*

open class Stat(
    private val uuid: UUID,

    var money: Int,
    var kills: Int,
    var wins: Int,
    var games: Int,
    var lootbox: Int,
    var lootboxOpenned: Int,

    var lastGenerationTime: Long = System.currentTimeMillis(),

    var timePlayedTotal: Long,

    var lastSeenName: String?,

    var claimedRewards: MutableList<Int>? = mutableListOf()
) : Unique {
    override fun getUuid(): UUID = uuid
}

