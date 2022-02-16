package me.reidj.bridgebuilders.user

import me.func.protocol.battlepass.BattlePassUserData
import me.reidj.bridgebuilders.achievement.Achievement
import me.reidj.bridgebuilders.battlepass.quest.BattlePassQuest
import me.reidj.bridgebuilders.battlepass.quest.QuestGenerator
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.impl.*
import java.util.*

data class Stat(
    val id: UUID,

    var money: Int,
    var kills: Int,
    var wins: Int,
    var games: Int,
    var lootbox: Int,
    var lootboxOpenned: Int,

    var progress: BattlePassUserData? = BattlePassUserData(10, false),
    var data: List<BattlePassQuest>? = QuestGenerator.generate(),
    var lastGenerationTime: Long = System.currentTimeMillis(),

    var achievement: MutableList<Achievement>,

    var donate: MutableList<DonatePosition>,
    var activeKillMessage: KillMessage,
    var activeParticle: StepParticle,
    var activeNameTag: NameTag,
    var activeCorpse: Corpse,
    var arrowParticle: ArrowParticle,
    var activeKit: StarterKit,

    var timePlayedTotal: Long,
    var lastEnter: Long,

    var lastSeenName: String?,

    var claimedRewards: MutableList<Int>? = mutableListOf()
)
