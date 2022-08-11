package me.reidj.bridgebuilders.util

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import me.func.mod.Anime
import me.func.mod.util.after
import me.func.protocol.EndStatus
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.packages.SaveUserPackage
import me.reidj.bridgebuilders.team.Team
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.entity.Firework
import ru.cristalix.core.formatting.Formatting

object WinUtil {

    fun check4win(): Boolean {
        if (activeStatus == Status.GAME || activeStatus == Status.END) {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                activeStatus = Status.END
                return true
            }
            // Если время вышло
            if (activeStatus.lastSecond * 20 == timer.time)
                return true
        }
        return false
    }

    fun end(team: Team) {
        team.players.mapNotNull { app.getUser(it) }.forEach {
            it.apply {
                player!!.sendMessage(Formatting.fine("Вы получили §e30 монет §fза победу."))
                stat.wins++
                giveMoney(30, false)
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
            clientSocket.write(SaveUserPackage(it.stat.uuid, it.stat))
        }
        Bukkit.getOnlinePlayers().mapNotNull { app.getUser(it) }.filter { it.player != null }.filter { !isSpectator(it.player!!) }.forEach {
            if (team.players.contains(it.stat.uuid))
                return@forEach
            it.giveMoney(15, false)
            it.player!!.sendMessage(Formatting.fine("Вы получили §e15 монет§f."))
            Anime.showEnding(
                it.player!!,
                EndStatus.LOSE,
                listOf("Блоков принесено:", "Игроков убито:"),
                listOf("${it.collectedBlocks}", "${it.kills}")
            )
            clientSocket.write(SaveUserPackage(it.stat.uuid, it.stat))
        }
        Bukkit.getOnlinePlayers().filter { !isSpectator(it) }.forEach {
            val user = app.getUser(it) ?: return@forEach
            user.stat.games++
            user.stat.realm = ""
            user.inGame = false
            if (Math.random() < 0.05) {
                user.stat.lootbox++
                B.bc(Formatting.fine("§e${it.player!!.name} §fполучил §bлутбокс§f!"))
            }
            clientSocket.write(SaveUserPackage(user.stat.uuid, user.stat))
            after(3 * 20) {
                userMap.clear()
                Bukkit.getOnlinePlayers().forEach { player ->
                    Cristalix.transfer(listOf(player.uniqueId), LOBBY_SERVER)
                }
            }
            after(5 * 20) { app.restart() }
        }
    }
}