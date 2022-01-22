package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import me.func.mod.Anime
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.donate.impl.NameTag
import me.reidj.bridgebuilders.mod.ModHelper
import me.reidj.bridgebuilders.mod.ModTransfer
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Material
import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import ru.cristalix.core.chat.IChatService
import ru.cristalix.core.chat.IChatView
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService
import java.util.concurrent.ExecutionException
import kotlin.math.min

object DefaultListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (activeStatus == Status.STARTING) {
            if (material == Material.WOOL) {
                teams.filter {
                    !it.players.contains(player.uniqueId) && it.color.woolData.toByte() == player.itemInHand.getData().data
                }.forEach { team ->
                    if (team.players.size >= slots / teams.size) {
                        player.sendMessage(Formatting.error("Ошибка! Команда заполнена."))
                        return@forEach
                    }
                    val prevTeam = teams.firstOrNull { it.players.contains(player.uniqueId) }
                    prevTeam?.players?.remove(player.uniqueId)
                    team.players.add(player.uniqueId)

                    // Удаляем у всех игрока из команды и добавляем в другую
                    val prevTeamIndex = teams.indexOf(prevTeam)
                    Bukkit.getOnlinePlayers()
                        .filter {
                            it.inventory.heldItemSlot == prevTeamIndex || it.inventory.heldItemSlot == teams.indexOf(
                                team
                            )
                        }
                        .forEach { showTeamList(app.getUser(it)) }
                    player.sendMessage(Formatting.fine("Вы выбрали команду: " + team.color.chatFormat + team.color.teamName))
                }
            } else if (material == Material.CLAY_BALL)
                Cristalix.transfer(listOf(player.uniqueId), LOBBY_SERVER)
        }
    }

    @EventHandler
    fun PlayerItemHeldEvent.handle() {
        if (activeStatus != Status.STARTING)
            return
        val newItem = player.inventory.getItem(newSlot)
        if (newItem != player.inventory.getItem(previousSlot))
            B.postpone(1) { showTeamList(app.getUser(player)) }
    }

    @EventHandler
    fun InventoryClickEvent.handle() {
        if (activeStatus == Status.STARTING)
            isCancelled = true
    }

    private fun showTeamList(user: User) {
        if (slots > 16)
            return

        val teamIndex = user.player!!.inventory.heldItemSlot
        val item = user.player!!.inventory.getItem(teamIndex)

        val template = ModTransfer()
            .integer(teamIndex)

        if (item != null && item.getType() == Material.WOOL) {
            val players = teams[teamIndex].players
            players.take(4).map { app.getUser(it) }.forEach {
                template.string(it.player!!.name)
            }
            repeat(4 - players.size) {
                template.string(if (it < slots / teams.size - players.size) " §7..." else "")
            }
        }
        template.send("bridge:team", user)
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        // Если мост не достроен откидывать от него игрока
        teams.forEach { team ->
            if (team.players.map { getByUuid(it) }
                    .sumOf { it.collectedBlocks } < 4096 && team.bridge.end.distanceSquared(player.location) < 42 * 42)
                player.velocity = team.spawn.toVector().subtract(player.location.toVector()).normalize()
        }
    }

    @EventHandler
    fun PlayerDropItemEvent.handle() {
        cancel = activeStatus == Status.STARTING
    }

    @EventHandler
    fun BlockPlaceEvent.handle() {
        if (block.type == Material.WORKBENCH || block.type == Material.FURNACE)
            return
        teams.forEach { team ->
            app.getBridge(team).forEach {
                if (it == block.location || team.spawn.distanceSquared(block.location) < 100 * 100)
                    isCancelled = true
            }
        }
    }

    @EventHandler
    fun BlockBreakEvent.handle() {
        teams.stream().forEach { team ->
            app.getBridge(team).forEach {
                if (it == block.location)
                    isCancelled = true
            }
            if (block.type == Material.BEACON) {
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
            }
            team.breakBlocks[block.location] = block.type
        }
    }
}