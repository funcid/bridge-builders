package me.reidj.bridgebuilders.listener

import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.data.Corpse
import me.reidj.bridgebuilders.data.KillMessage
import me.reidj.bridgebuilders.data.NameTag
import me.reidj.bridgebuilders.data.StarterKit
import me.reidj.bridgebuilders.donate.impl.StepParticle
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.isSpectator
import me.reidj.bridgebuilders.packages.SaveUserPackage
import me.reidj.bridgebuilders.packages.StatPackage
import me.reidj.bridgebuilders.user.Stat
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.userMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.event.player.*
import java.util.concurrent.TimeUnit

object GlobalListeners : Listener {

    @EventHandler
    fun AsyncPlayerPreLoginEvent.handle() {
        if (userMap.contains(uniqueId))
            return
        try {
            val statPackage = clientSocket.writeAndAwaitResponse<StatPackage>(StatPackage(uniqueId)).get(5, TimeUnit.SECONDS)
            var stat = statPackage.stat
            if (stat == null)
                stat = Stat(
                    uniqueId,
                    "",
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    ArrayList(),
                    HashSet(),
                    ArrayList(),
                    KillMessage.NONE,
                    me.reidj.bridgebuilders.data.StepParticle.NONE,
                    NameTag.NONE,
                    Corpse.NONE,
                    StarterKit.NONE,
                    0.0,
                    0.0,
                    0L,
                    0.0,
                    0.0,
                    0L,
                    0L,
                    0,
                    true,
                    false
                )

            if (stat.uuid == null) stat.uuid = uniqueId

            if (stat.realm == null) stat.realm = ""

            if (stat.donate == null) stat.donate = ArrayList()

            if (stat.donates == null) stat.donates = HashSet()

            if (stat.achievement == null) stat.achievement = ArrayList()

            if (stat.activeKillMessage == null) stat.activeKillMessage = KillMessage.NONE

            if (stat.activeParticle == null) stat.setActiveParticle(me.reidj.bridgebuilders.data.StepParticle.NONE)

            if (stat.activeNameTag == null) stat.activeNameTag = NameTag.NONE

            if (stat.activeCorpse == null) stat.activeCorpse = Corpse.NONE

            if (stat.activeKit == null) stat.activeKit = StarterKit.NONE

            if (stat.isApprovedResourcepack == null) stat.isApprovedResourcepack = true

            if (stat.isBan == null) stat.isBan = false

            if (stat.gameLockTime == null) stat.gameLockTime = 0

            if (stat.banTime == null) stat.banTime = 0.0

            if (stat.leaveTime == null) stat.leaveTime = 0.0

            if (stat.timePlayedTotal == null) stat.timePlayedTotal = 0L

            if (stat.gameExitTime == null) stat.gameExitTime = 0

            if (stat.dailyClaimTimestamp == null) stat.dailyClaimTimestamp = 0L

            if (stat.lastEnter == null) stat.lastEnter = 0L

            if (stat.dailyTimestamp == null) stat.dailyTimestamp = 0.0

            if (stat.lastEnterTime == null) stat.lastEnterTime = 0.0

            if (stat.rewardStreak == null) stat.rewardStreak = 0

            userMap[uniqueId] = User(stat)
        } catch (ex: Exception) {
            userMap.remove(uniqueId)
            disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Нам не удалось загрузить Вашу статистику")
            loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER;
            ex.printStackTrace()
        }
    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        val uuid = player.uniqueId
        val user = userMap[uuid] ?: return
        if (!user.inGame)
            userMap.remove(uuid)
        clientSocket.write(SaveUserPackage(uuid, user.stat))
    }

    @EventHandler
    fun BlockRedstoneEvent.handle() = apply { newCurrent = oldCurrent }

    @EventHandler
    fun PlayerInteractEntityEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockFadeEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockSpreadEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun EntityChangeBlockEvent.handle() = apply {
        isCancelled = true
        block.state.update(false, false)
    }

    @EventHandler
    fun BlockGrowEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockPhysicsEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun PlayerArmorStandManipulateEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun PlayerSwapHandItemsEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockFromToEvent.handle() {
        val id = block.typeId
        isCancelled = id == 8 || id == 9
    }

    @EventHandler
    fun ExplosionPrimeEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (isSpectator(player))
            return
        val user = getByPlayer(player) ?: return
        val particle = user.stat.activeParticle
        if (particle != me.reidj.bridgebuilders.data.StepParticle.NONE && player.world != null && player != null && player.location != null) {
            val location = player.location
            player.world.spawnParticle(
                particle?.let { StepParticle.valueOf(it.name).type },
                location.x,
                location.y + 0.2,
                location.z,
                1
            )
        }
    }
}