package me.reidj.node.game

import me.func.mod.Anime
import me.func.mod.util.after
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.isSpectator
import me.reidj.node.activeStatus
import me.reidj.node.teams
import me.reidj.node.timer.Status
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

private const val COOL_DOWN = 120

class PlayerMoveHandler(private val game: BridgeGame) : Listener {

    @EventHandler
    fun PlayerMoveEvent.handle() {
        val location = player.location

        if (activeStatus == Status.STARTING && location.y <= 2)
            player.teleport(game.getSpawnLocation())

        if (teams.any { it.bridge.end.world != location.world })
            return

        // Если мост не достроен откидывать от него игрока
        teams.filter { it.getCountBlocksTeam(game.mapType) && it.bridge.end.distanceSquared(location) < 29 * 12 && !player.isSpectator() }
            .forEach { player.velocity = it.spawn.toVector().subtract(location.toVector()).normalize() }

        // Телепортация на вражескую базу
        if (location.block.getRelative(BlockFace.DOWN).type == Material.SEA_LANTERN) {
            val uuid = player.uniqueId
            val team = teams.firstOrNull { uuid in it.players } ?: return
            val user = getUser(player) ?: return

            if (!user.isTeleportAvailable)
                return

            if (location.distanceSquared(team.teleport) < 2 * 2) {
                val enemyTeams = teams.filter { uuid !in it.players }

                if (enemyTeams.isEmpty())
                    return

                val enemyTeam = enemyTeams.random()

                enemyTeam.baseTeleport(player)
                user.isTeleportAvailable = false
                Anime.reload(player, COOL_DOWN.toDouble(), "До следующего телепорта", 42, 102, 240)

                after((COOL_DOWN * 20).toLong()) {
                    user.isTeleportAvailable = true
                    game.teleportAvailable(player)
                }

                enemyTeam.players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
                    Anime.bigTitle(it, "§cВаша база была атакована!")
                    it.sendMessage("${enemyTeam.commandPrefix} §cВаша база была атакована!")
                    it.playSound(it.location, Sound.ENTITY_ENDERDRAGON_GROWL, 1f, 1f)
                }
            } else {
                team.baseTeleport(player)
            }
        }
    }
}