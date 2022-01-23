package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import me.func.mod.Anime
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.getByUuid
import me.reidj.bridgebuilders.mod.ModHelper
import me.reidj.bridgebuilders.teams
import me.reidj.bridgebuilders.timer
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min

object BlockHandler : Listener {

    @EventHandler
    fun BlockPlaceEvent.handle() {
        teams.forEach { team ->
            if (block.location.distanceSquared(teams.filter { it.players.contains(player.uniqueId) }[0].spawn) > 50 * 50
                || app.getBridge(team).contains(block.location)
            )
                isCancelled = true
        }
    }

    private val has = timer.time / 20 >= 900

    @EventHandler
    fun BlockBreakEvent.handle() {
        if (block.type == Material.IRON_ORE) {
            block.type = Material.AIR
            player.inventory.addItem(ItemStack(Material.IRON_INGOT, if (has) 2 else 1))
        } else if (block.type == Material.GOLD_ORE) {
            block.type = Material.AIR
            player.inventory.addItem(ItemStack(Material.GOLD_ORE, if (has) 2 else 1))
        }
        // Если прошло 15 минут с начала игры будет выпадать больше предметов
        if (has) {
            block.drops.forEach {
                Bukkit.getOnlinePlayers().forEach { player ->
                    Anime.alert(
                        player,
                        "Ускорение",
                        "Увеличено выпадение ресурсов"
                    )
                }
                it.setAmount(it.getAmount() + 1)
                block.world.dropItemNaturally(block.location, it)
            }
            dropItems = false
        }
        teams.stream().forEach { team ->
            if (app.getBridge(team).contains(block.location))
                isCancelled = true
            else if (block.type == Material.BEACON && app.getCountBlocksTeam(team))
                isCancelled = true
            if (block.type == Material.BEACON && app.getCountBlocksTeam(team)) {
                if (team.players.contains(player.uniqueId)) {
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
                    B.postpone(5 * 20) { app.restart() }
                } else {
                    team.players.forEach {
                        getByUuid(it).player?.let { player -> Anime.title(player, "§aПОРАЖЕНИЕ\n§aвы проиграли!") }
                    }
                    return@forEach
                }
            }
            team.breakBlocks[block.location] = block.typeId to block.data
        }
    }
}