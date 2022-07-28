package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import me.func.mod.Anime
import me.func.mod.Npc
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.teams
import me.reidj.bridgebuilders.util.WinUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object BlockHandler : Listener {

    val placedBlocks = mutableMapOf<Location, Pair<Int, Byte>>()

    @EventHandler
    fun BlockPlaceEvent.handle() {
        placedBlocks[block.location] = block.typeId to block.data
        if (teams.all {
                block.location.distanceSquared(it.spawn) > 60 * 72 || app.getBridge(it).contains(block.location) ||
                        block.location.distanceSquared(it.spawn) < 4 * 4
            } || isNpcBlock(block.location))
            isCancelled = true
    }

    @EventHandler
    fun BlockBreakEvent.handle() {
        val allTeams = teams.filter { it.players.contains(player.uniqueId) }
        val team = allTeams[0]
        if (teams.any { app.getBridge(it).contains(block.location) }) {
            isCancelled = true
            return
        } else if (allTeams.isNotEmpty() && block.type == Material.BEACON && app.getCountBlocksTeam(team)) {
            isCancelled = true
            return
        } else if (teams.any { block.location.distanceSquared(it.spawn) < 4 * 4 }) {
            isCancelled = true
            return
        } else if (block.type == Material.SEA_LANTERN) {
            isCancelled = true
            return
        } else if (isNpcBlock(block.location)) {
            isCancelled = true
        }
        if (allTeams.isNotEmpty() && block.type == Material.BEACON && !app.getCountBlocksTeam(team)) {
            val winner = teams.filter { it.players.contains(player.uniqueId) }[0]
            Bukkit.getOnlinePlayers().forEach {
                Anime.killboardMessage(
                    it,
                    "Победила команда ${winner.color.chatFormat + winner.color.teamName}"
                )
            }
            B.bc(" ")
            B.bc("§b―――――――――――――――――")
            B.bc("" + winner.color.chatFormat + winner.color.teamName + " §f победили!")
            B.bc(" ")
            B.bc("§e§lТОП ПРИНЕСЁННЫХ БЛОКОВ")
            winner.players.mapNotNull { app.getUser(it) }.sortedBy { -it.collectedBlocks }
                .subList(0, min(3, winner.players.size))
                .forEachIndexed { index, user ->
                    var playerName = ""
                    playerName = if (user.player == null) "ERROR" else user.player!!.name
                    B.bc(" §l${index + 1}. §e" + playerName + " §с" + user.collectedBlocks + " блоков принесено")
                }
            B.bc("§b―――――――――――――――――")
            B.bc(" ")

            WinUtil.end(winner)

            B.postpone(5 * 20) { app.restart() }
            return
        }
        if (block.type == Material.IRON_ORE) {
            block.type = Material.AIR
            player.inventory.addItem(ItemStack(Material.IRON_INGOT))
        } else if (block.type == Material.GOLD_ORE) {
            block.type = Material.AIR
            player.inventory.addItem(ItemStack(Material.GOLD_ORE))
        } else if (block.type == Material.DIAMOND_ORE) {
            block.type = Material.AIR
            player.inventory.addItem(ItemStack(Material.DIAMOND))
        }
        if (placedBlocks.any { place -> team.breakBlocks.any { it.value == place.value } })
            return
        when (val idAndData = block.typeId to block.data) {
            15 to 0.toByte() -> {
                team.breakBlocks[block.location] = idAndData
            }
            56 to 0.toByte() -> {
                team.breakBlocks[block.location] = idAndData
            }
            16 to 0.toByte() -> {
                team.breakBlocks[block.location] = idAndData
            }
            14 to block.data -> team.breakBlocks[block.location] = idAndData
            17 to block.data -> team.breakBlocks[block.location] = idAndData
            5 to block.data -> team.breakBlocks[block.location] = idAndData
            17 to block.data -> team.breakBlocks[block.location] = idAndData
            1 to 5.toByte() -> team.breakBlocks[block.location] = idAndData
            12 to 0.toByte() -> team.breakBlocks[block.location] = idAndData
            159 to 10.toByte() -> team.breakBlocks[block.location] = idAndData
        }
    }

    private fun isNpcBlock(location: Location) = Npc.npcs.any {
        sqrt(
            (it.value.data.x - location.x).pow(2.0) + (it.value.data.y - location.y).pow(
                2.0
            ) + (it.value.data.z - location.z).pow(2.0)
        ) <= 5
    }

}