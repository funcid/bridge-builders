package me.reidj.bridgebuilders.user

import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import me.func.protocol.battlepass.BattlePassUserData
import me.reidj.bridgebuilders.battlepass.quest.QuestGenerator
import me.reidj.bridgebuilders.donate.impl.*
import org.bukkit.entity.Player
import java.util.*

class User(session: KensukeSession, stat: Stat?) : IBukkitKensukeUser {

    var collectedBlocks = 0
    var kills = 0
    var activeHand = false

    var stat: Stat
    private var player: Player? = null
    override fun setPlayer(p0: Player?) {
        if (p0 != null) {
            player = p0
        }
    }

    override fun getPlayer() = player

    private var session: KensukeSession
    override fun getSession() = session

    init {
        if (stat == null) {
            this.stat = Stat(
                UUID.fromString(session.userId),
                0,
                0,
                0,
                0,
                0,
                0,
                BattlePassUserData(10, false),
                QuestGenerator.generate(),
                System.currentTimeMillis(),
                mutableListOf(),
                arrayListOf(
                    KillMessage.NONE,
                    StepParticle.NONE,
                    NameTag.NONE,
                    Corpse.NONE,
                ),
                KillMessage.NONE,
                StepParticle.NONE,
                NameTag.NONE,
                Corpse.NONE,
                StarterKit.NONE,
                0,
                "",
                mutableListOf()
            )
        } else {
            if (stat.money == null)
                stat.money = 0
            if (stat.kills == null)
                stat.kills = 0
            if (stat.lootbox == null)
                stat.lootbox = 0
            if (stat.lootboxOpenned == null)
                stat.lootboxOpenned = 0
            if (stat.achievement == null || stat.achievement.isEmpty())
                stat.achievement = mutableListOf()
            if (stat.donate == null || stat.donate.isEmpty())
                stat.donate = arrayListOf(
                    KillMessage.NONE,
                    StepParticle.NONE,
                    NameTag.NONE,
                    Corpse.NONE,
                )
            if (stat.activeKillMessage == null)
                stat.activeKillMessage = KillMessage.NONE
            if (stat.activeParticle == null)
                stat.activeParticle = StepParticle.NONE
            if (stat.activeNameTag == null)
                stat.activeNameTag = NameTag.NONE
            if (stat.activeCorpse == null)
                stat.activeCorpse = Corpse.NONE
            if (stat.timePlayedTotal == null)
                stat.timePlayedTotal = 0
            if (stat.activeKit == null)
                stat.activeKit = StarterKit.NONE
            if (stat.progress == null)
                stat.progress = BattlePassUserData(10, false)
            if (stat.data == null)
                stat.data = QuestGenerator.generate()
            if (stat.lastGenerationTime == null)
                stat.lastGenerationTime = System.currentTimeMillis()
            if (stat.claimedRewards == null)
                stat.claimedRewards = mutableListOf()
            this.stat = stat
        }
        this.session = session
    }

    fun giveMoney(money: Int) = changeMoney(money)


    fun minusMoney(money: Int) = changeMoney(-money)


    private fun changeMoney(dMoney: Int) {
        stat.money += dMoney
        me.func.mod.conversation.ModTransfer(stat.money).send("bridge:balance", player)
    }
}