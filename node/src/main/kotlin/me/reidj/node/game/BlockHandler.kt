package me.reidj.node.game

import me.func.mod.Anime
import me.reidj.bridgebuilders.getUser
import me.reidj.node.block_regeneration.RegenerationManager
import me.reidj.node.teams
import org.bukkit.Bukkit
import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import java.lang.Integer.min

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class BlockHandler(private val game: BridgeGame) : Listener {

    @EventHandler
    fun BlockPlaceEvent.handle() {
        isCancelled = isCancelled(block) || teams.all { block.location.distanceSquared(it.spawn) > 60 * 72 }
    }

    @EventHandler
    fun BlockBreakEvent.handle() {
        val team = teams.firstOrNull { player.uniqueId in it.players } ?: return
        val type = block.type
        val hasBlock = type == BEACON
        val hasBlockCount = team.getCountBlocksTeam(game.mapType)

        isCancelled = isCancelled(block) || (hasBlock && hasBlockCount)

        RegenerationManager.addBlock(block)

        // Конец игры
        if (hasBlock && !hasBlockCount) {
            val chatFormat = team.color.chatFormat
            val teamName = team.color.teamName
            Bukkit.getOnlinePlayers()
                .forEach { Anime.killboardMessage(it, "Победила команда ${chatFormat + teamName}") }
            Bukkit.broadcastMessage(" ")
            Bukkit.broadcastMessage("§b―――――――――――――――――")
            Bukkit.broadcastMessage("$chatFormat$teamName §fпобедили!")
            Bukkit.broadcastMessage(" ")
            Bukkit.broadcastMessage("§e§lТОП ПРИНЕСЁННЫХ БЛОКОВ")
            team.players.mapNotNull { getUser(it) }.sortedBy { -it.collectedBlocks }
                .subList(0, min(3, team.players.size))
                .forEachIndexed { index, user ->
                    val playerName: String = if (user.cachedPlayer == null) "ERROR" else user.cachedPlayer!!.name
                    Bukkit.broadcastMessage(" §l${index + 1}. §e" + playerName + " §с" + user.collectedBlocks + " блоков принесено")
                }
            Bukkit.broadcastMessage("§b―――――――――――――――――")
            Bukkit.broadcastMessage(" ")

            ru.cristalix.core.karma.IKarmaService.get().enableGG { true }

            game.end(team)
        }

        // Автопереплавка руды
        when (type) {
            IRON_ORE, GOLD_ORE, DIAMOND_ORE -> block.type = AIR
            else -> block
        }
        player.inventory.addItem(
            ItemStack(
                when (type) {
                    IRON_ORE -> IRON_INGOT
                    GOLD_ORE -> GOLD_INGOT
                    DIAMOND_ORE -> DIAMOND
                    else -> AIR
                }
            )
        )
    }

    private fun isCancelled(block: Block): Boolean {
        val location = block.location
        return teams.any {
            it.bridge.blockOfBridge(
                block,
                game.mapType
            ) || location.distanceSquared(it.spawn) < 2 || block.type == SEA_LANTERN || game.blockNextToNpc(
                location
            )
        }
    }
}