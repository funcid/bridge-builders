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
import org.bukkit.inventory.ItemStack
import kotlin.math.min

object BlockHandler : Listener {

    @EventHandler
    fun BlockPlaceEvent.handle() {
        if (teams.all {
                block.location.distanceSquared(it.spawn) > 60 * 72 || app.getBridge(it).contains(block.location)
            })
            isCancelled = true
    }

    @EventHandler
    fun BlockBreakEvent.handle() {
        val team = teams.filter { it.players.contains(player.uniqueId) }[0]
        when (val idAndData = block.typeId to block.data) {
            15 to 0.toByte() -> {
                //BattlePassUtil.update(player, BREAK, 1)
                team.breakBlocks[block.location] = idAndData
            }
            56 to 0.toByte() -> {
                //BattlePassUtil.update(player, BREAK, 1)
                team.breakBlocks[block.location] = idAndData
            }
            16 to 0.toByte() -> {
                //BattlePassUtil.update(player, BREAK, 1)
                team.breakBlocks[block.location] = idAndData
            }
            14 to block.data -> team.breakBlocks[block.location] = idAndData
            17 to block.data -> team.breakBlocks[block.location] = idAndData
            5 to block.data -> team.breakBlocks[block.location] = idAndData
            17 to block.data -> team.breakBlocks[block.location] = idAndData
            1 to 5.toByte() -> team.breakBlocks[block.location] = idAndData
            12 to 0.toByte() -> team.breakBlocks[block.location] = idAndData
        }
        if (app.getBridge(team).contains(block.location)) {
            isCancelled = true
            return
        } else if (block.type == Material.BEACON && app.getCountBlocksTeam(team)) {
            isCancelled = true
            return
        }
        if (block.type == Material.BEACON && !app.getCountBlocksTeam(team)) {
            //BattlePassUtil.update(player, BREAK, 1)
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

                    //BattlePassUtil.update(user.player!!, WIN, 1)
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
                return
            }
        }
        val has = activeStatus.now(timer.time) / 20 >= 900
        if (block.type == Material.IRON_ORE) {
            block.type = Material.AIR
            player.inventory.addItem(ItemStack(Material.IRON_INGOT, if (has) 3 else 1))
        } else if (block.type == Material.GOLD_ORE) {
            block.type = Material.AIR
            player.inventory.addItem(ItemStack(Material.GOLD_ORE, if (has) 3 else 1))
        }
        // Если прошло 15 минут с начала игры будет выпадать больше предметов
        if (has) {
            block.drops.forEach {
                it.setAmount(it.getAmount() + 2)
                block.world.dropItemNaturally(block.location, it)
            }
            dropItems = false
        }
    }
}