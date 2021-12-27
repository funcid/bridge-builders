package me.reidj.bridgebuilders.user

import me.func.commons.donate.DonatePosition
import me.func.commons.donate.impl.*
import me.reidj.bridgebuilders.achievement.Achievement
import java.util.*

data class Stat(
    val id: UUID,

    var money: Int,
    var kills: Int,
    var wins: Int,
    var games: Int,
    var lootbox: Int,
    var lootboxOpenned: Int,
    var rewardStreak: Int,

    var achievement: MutableList<Achievement>,

    var donate: MutableList<DonatePosition>,
    var activeKillMessage: KillMessage,
    var activeParticle: StepParticle,
    var activeNameTag: NameTag,
    var activeCorpse: Corpse,
    var arrowParticle: ArrowParticle,

    var timePlayedTotal: Long,
    var lastEnter: Long,
    var dailyClaimTimestamp: Long,

    var lastSeenName: String?,
)
