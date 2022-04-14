package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import ru.cristalix.core.formatting.Formatting

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
                        .mapNotNull { app.getUser(it) }
                        .forEach { showTeamList(it) }
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
            B.postpone(1) { app.getUser(player)?.let { showTeamList(it) } }
    }

    /*@EventHandler
    fun CraftItemEvent.handle() {
        val player = whoClicked as Player
        //BattlePassUtil.update(player, CRAFT, 1)
        val has = recipe.result
        if (has.getType().name.endsWith("AXE"))
            has.
        if (has == Material.DIAMOND_BOOTS || has == Material.IRON_CHESTPLATE || has == Material.DIAMOND_HELMET
            || has == Material.IRON_LEGGINGS || has == Material.DIAMOND_SWORD)
            BattlePassUtil.update(player, CRAFT, 1)
    }*/

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

        val template = me.func.mod.conversation.ModTransfer()
            .integer(teamIndex)

        if (item != null && item.getType() == Material.WOOL) {
            val players = teams[teamIndex].players
            players.take(4).mapNotNull { app.getUser(it) }.forEach {
                template.string(it.player!!.name)
            }
            repeat(4 - players.size) {
                template.string(if (it < slots / teams.size - players.size) " §7..." else "")
            }
        }
        template.send("bridge:team", user.player)
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (activeStatus == Status.STARTING && player.location.block.y <= 2)
            player.teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5))
        // Если мост не достроен откидывать от него игрока
        teams.forEach { team ->
            if (app.getCountBlocksTeam(team) && team.bridge.end.distanceSquared(player.location) < 29 * 12 && !app.isSpectator(
                    player
                )
            )
                player.velocity = team.spawn.toVector().subtract(player.location.toVector()).normalize()
        }
    }

    @EventHandler
    fun PlayerDropItemEvent.handle() = apply { cancel = activeStatus == Status.STARTING }
}