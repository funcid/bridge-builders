package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import implario.ListUtils
import me.reidj.bridgebuilders.*
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.realm.RealmId
import java.util.stream.Collectors

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
        if (player.location.subtract(0.0, 1.0, 0.0).block.type == Material.EMERALD_BLOCK && timer.time / 20 >= 180) {
            val team = ListUtils.random(teams.stream()
                .filter { !it.players.contains(player.uniqueId) }
                .collect(Collectors.toList()))
            if (team.isActiveTeleport) {
                team.isActiveTeleport = false
                player.teleport(team.location)
                B.postpone(20 * 2) { team.isActiveTeleport = true }
            }
        }
    }
}