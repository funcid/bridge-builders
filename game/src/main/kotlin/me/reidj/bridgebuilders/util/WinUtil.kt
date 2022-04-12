package me.reidj.bridgebuilders.util

import me.func.mod.Anime
import me.func.protocol.EndStatus
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.data.Team
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.Firework
import ru.cristalix.core.formatting.Formatting

object WinUtil {

    fun check4win(): Boolean {
        if (activeStatus != Status.GAME)
            return false
        if (teams.all { it.players.size == 0 }) {
            activeStatus = Status.END
            return true
        }
        // Если время вышло
        if (activeStatus.lastSecond * 20 == timer.time)
            return true
        return false
    }

    fun end(winner: User, team: Team) {
        winner.apply {
            player!!.sendMessage(Formatting.fine("Вы получили §e10 монет §fза победу."))
            stat.wins++
            giveMoney(10)
            Anime.showEnding(
                player!!,
                EndStatus.WIN,
                listOf("Блоков принесено:", "Игроков убито:"),
                listOf("$collectedBlocks", "$kills")
            )
            val firework = player!!.world!!.spawn(player!!.location, Firework::class.java)
            val meta = firework.fireworkMeta
            meta.addEffect(
                FireworkEffect.builder()
                    .flicker(true)
                    .trail(true)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .with(FireworkEffect.Type.BALL)
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.YELLOW)
                    .withColor(Color.GREEN)
                    .withColor(Color.WHITE)
                    .build()
            )
            meta.power = 0
            firework.fireworkMeta = meta
        }
        Bukkit.getOnlinePlayers().mapNotNull { app.getUser(it) }.forEach {
            if (team.players.contains(it.stat.id))
                return@forEach
            it.giveMoney(5)
            it.player!!.sendMessage(Formatting.fine("Вы получили §e5 монет§f."))
            Anime.showEnding(
                it.player!!,
                EndStatus.LOSE,
                listOf("Блоков принесено:", "Игроков убито:"),
                listOf("${it.collectedBlocks}", "${it.kills}")
            )
        }
    }
}