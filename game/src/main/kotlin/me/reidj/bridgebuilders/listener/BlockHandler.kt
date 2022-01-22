package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import me.func.mod.Anime
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.mod.ModHelper
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import kotlin.math.min

object BlockHandler : Listener {

    @EventHandler
    fun BlockPlaceEvent.handle() {
        if (block.location.distanceSquared(teams.filter { it.players.contains(player.uniqueId) }[0].spawn) > 50 * 50)
            isCancelled = true
    }

    @EventHandler
    fun BlockBreakEvent.handle() {
        teams.stream().forEach { team ->
            app.getBridge(team).forEach {
                if (it == block.location)
                    isCancelled = true
            }
            if (block.type == Material.BEACON && app.getCountBlocksTeam(team)) {
                if (team.players.contains(player.uniqueId)) {
                    activeStatus = Status.END
                    ModHelper.allNotification("Победила команда ${team.color.chatFormat + team.color.teamName}")
                    B.bc(" ")
                    B.bc("§b―――――――――――――――――")
                    B.bc("" + team.color.chatFormat + team.color.teamName + " §f победили!")
                    B.bc(" ")
                    B.bc("§e§lТОП ПРИНЕСЁННЫХ БЛОКОВ")
                    team.players.map { getByUuid(it) }.sortedBy { -it.collectedBlocks }
                        .subList(0, min(3, team.players.size))
                        .forEachIndexed { index, user ->
                            B.bc(" §l${index + 1}. §e" + user.player?.name + " §с" + user.collectedBlocks + " блоков принесено")
                        }
                    B.bc("§b―――――――――――――――――")
                    B.bc(" ")
                    team.players.forEach { uuid ->
                        val user = app.getUser(uuid)
                        user.stat.wins++
                        user.player?.let { player -> Anime.title(player, "§aПОБЕДА\n§aвы выиграли!") }
                        val firework = user.player!!.world!!.spawn(user.player!!.location, Firework::class.java)
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
                } else {
                    team.players.forEach {
                        getByUuid(it).player?.let { player -> Anime.title(player, "§aПОРАЖЕНИЕ\n§aвы проиграли!") }
                    }
                    return@forEach
                }
            } else {
                isCancelled = true
            }
            team.breakBlocks[block.location] = block.type
        }
    }
}