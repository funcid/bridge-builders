package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.bukkit.Cycle
import clepto.cristalix.Cristalix
import implario.ListUtils
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.mod.ModHelper
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.realm.RealmId

object DefaultListener : Listener {

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (activeStatus == Status.STARTING) {
            if (material == Material.CLAY_BALL) {
                Cristalix.transfer(listOf(player.uniqueId), RealmId.of(HUB))
                return
            }
            if (material == Material.WOOL) {
                teams.filter {
                    !it.players.contains(player.uniqueId) && it.color.woolData.toByte() == player.itemInHand.getData().data
                }.forEach { team ->
                    if (team.players.size >= slots / teams.size) {
                        player.sendMessage(Formatting.error("Ошибка! Команда заполена."))
                        return@forEach
                    }
                    teams.forEach { it.players.remove(player.uniqueId) }
                    team.players.add(player.uniqueId)
                    player.sendMessage(Formatting.fine("Вы выбрали команду: " + team.color.chatFormat + team.color.teamName))
                }
            }
        }
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (player.location.subtract(0.0, 1.0, 0.0).block.type == Material.EMERALD_BLOCK) {
            val team = ListUtils.random(teams)
            if (timer.time >= 180 && team.isActiveTeleport) {
                team.isActiveTeleport = false
                player.teleport(team.location)
                B.postpone(2 * 20) { team.isActiveTeleport = true }
            }
        }
    }

    lateinit var toDelete: List<ItemStack>

    @EventHandler
    fun PlayerDeathEvent.handle() {
        cancelled = true

        val player = entity as Player

        if (player.gameMode == GameMode.SPECTATOR)
            return

        player.gameMode = GameMode.SPECTATOR

        Cycle.run(20, 5) {
            if (it == 5) {
                player.gameMode = GameMode.SURVIVAL
                teams.stream()
                    .filter { team -> team.players.contains(player.uniqueId) }
                    .forEach { team -> player.teleport(team.location) }
                Cycle.exit()
            }
            ModHelper.sendTitle(app.getUser(player), "До возрождения ${5 - it}")
        }

        player.inventory.contents.toList().stream()
            .filter { it != null }
            .forEach {
                when (it.amount > 4) {
                    true -> it.amount -= 2
                    false -> toDelete = listOf(it)
                }
            }

        toDelete.forEach { player.inventory.remove(it) }
    }
}