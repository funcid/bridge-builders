package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import implario.ListUtils
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.mod.ModHelper
import me.reidj.bridgebuilders.mod.ModTransfer
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import ru.cristalix.core.formatting.Formatting
import java.util.stream.Collectors

object DefaultListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (activeStatus == Status.STARTING && material == Material.WOOL) {
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
                    .forEach { showTeamList(app.getUser(it)!!) }
                player.sendMessage(Formatting.fine("Вы выбрали команду: " + team.color.chatFormat + team.color.teamName))
            }
        }
        if (action == Action.LEFT_CLICK_AIR) {
            teams.filter { it.players.contains(player.uniqueId) }
                .forEach { team ->
                    team.requiredBlocks.entries.forEachIndexed { index, block ->
                        val itemHand = player.itemInHand
                        if (itemHand.getType().getId() == block.key) {
                            val must = block.value.needTotal - block.value.collected
                            if (must == 0) {
                                ModHelper.notification(
                                    getByUuid(player.uniqueId),
                                    Formatting.error("Мне больше не нужен этот ресурс")
                                )
                                return@forEach
                            } else {
                                block.value.collected = block.value.needTotal - maxOf(0, must - itemHand.getAmount())
                                itemHand.setAmount(itemHand.getAmount() - must)
                            }
                            team.players.forEach { uuid ->
                                ModTransfer()
                                    .integer(index)
                                    .integer(block.value.needTotal)
                                    .integer(block.value.collected)
                                    .send("bridge:tabupdate", getByUuid(uuid))
                            }
                            player.updateInventory()
                        }
                    }
                }
        }
    }

    @EventHandler
    fun PlayerItemHeldEvent.handle() {
        if (activeStatus != Status.STARTING)
            return
        val newItem = player.inventory.getItem(newSlot)
        if (newItem != player.inventory.getItem(previousSlot))
            B.postpone(1) { showTeamList(app.getUser(player)!!) }
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
                template.string(it!!.player!!.name)
            }
            repeat(4 - players.size) {
                template.string(if (it < slots / teams.size - players.size) " §7..." else "")
            }
        }

        template.send("bridge:team", user)
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (player.location.subtract(0.0, 1.0, 0.0).block.type == Material.EMERALD_BLOCK && timer.time / 20 >= 180) {
            teams.filter { it.players.contains(player.uniqueId) }
                .forEach {
                    if (it.isActiveTeleport) {
                        it.isActiveTeleport = false
                        player.teleport(ListUtils.random(teams.stream()
                            .filter { team -> !team.players.contains(player.uniqueId) }
                            .collect(Collectors.toList())).spawn)
                        B.postpone(20 * 180) {
                            it.isActiveTeleport = true
                            it.players.forEach { uuid ->
                                ModHelper.notification(
                                    getByUuid(uuid),
                                    "Телепорт на чужие базы теперь §aдоступен"
                                )
                            }
                        }
                    }
                }
        }
    }

    @EventHandler
    fun BlockBreakEvent.handle() {
        teams.stream()
            .filter { it.players.contains(player.uniqueId) }
            .forEach { it.breakBlocks[block.location] = block.type }
    }
}