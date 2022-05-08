package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import me.func.mod.Anime
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.team.Team
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.realm.IRealmService
import java.lang.Math.cos
import java.lang.Math.sin

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

    @EventHandler
    fun InventoryClickEvent.handle() {
        if (activeStatus == Status.STARTING)
            isCancelled = true
    }

    @EventHandler
    fun FoodLevelChangeEvent.handle() = apply { if (activeStatus == Status.STARTING) level = 20 }

    private fun showTeamList(user: User) {
        if (slots > 16)
            return

        val teamIndex = user.player!!.inventory.heldItemSlot
        val item = user.player!!.inventory.getItem(teamIndex)

        val template = me.func.mod.conversation.ModTransfer().integer(teamIndex)

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
        val location = player.location
        if (activeStatus == Status.STARTING && player.location.block.y <= 2)
            player.teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5))
        // Если мост не достроен откидывать от него игрока
        if (teams.none { it.players.contains(player.uniqueId) })
            return
        teams.forEach { team ->
            if (app.getCountBlocksTeam(team) && team.bridge.end.distanceSquared(player.location) < 29 * 12 && !isSpectator(
                    player
                )
            ) {
                for (i in 0..60)
                    player.spawnParticle(
                        Particle.SPELL_INSTANT,
                        sin(Math.toRadians(location.x * 6)),
                        location.x,
                        cos(Math.toRadians(location.z * 6)),
                        1
                    )
                player.velocity = team.spawn.toVector().subtract(player.location.toVector()).normalize()
            }
        }
        // Телепортация на вражескую базу
        if (location.subtract(0.0, 1.0, 0.0).block.type == Material.BEDROCK) {
            val playerTeam = teams.filter { team -> team.players.contains(player.uniqueId) }[0]
            if (!playerTeam.isActiveTeleport)
                return
            if (player.location.distanceSquared(playerTeam.teleport) < 4 * 4) {
                if (teams.any { enemy -> !enemy.players.contains(player.uniqueId) && enemy.isActiveTeleport }) {
                    val enemyTeam =
                        teams.filter { enemy -> !enemy.players.contains(player.uniqueId) && enemy.isActiveTeleport }
                            .random()
                    app.teleportAtBase(enemyTeam, player)
                    enemyTeam.players.mapNotNull { uuid -> Bukkit.getPlayer(uuid) }.forEach { enemy ->
                        enemy.playSound(
                            player.location,
                            Sound.ENTITY_ENDERDRAGON_GROWL,
                            1f,
                            1f
                        )
                    }
                }
            } else {
                app.teleportAtBase(playerTeam, player)
            }
            playerTeam.isActiveTeleport = false

            // Ставлю полоску куллдауна
            displayCoolDownBar(playerTeam)

            B.postpone(120 * 20) {
                playerTeam.isActiveTeleport = true
                // Отправляю сообщение о том что телепорт доступен
                teleportAvailable(playerTeam)
            }
        }
    }

    @EventHandler
    fun PlayerDropItemEvent.handle() = apply { cancel = activeStatus == Status.STARTING }

    private fun displayCoolDownBar(team: Team) {
        team.players.mapNotNull { Bukkit.getPlayer(it) }
            .forEach {
                Anime.reload(it, 0.1, "До следующего телепорта", 42, 102, 240)
                B.postpone(20 * 2) { Anime.reload(it, 120.0, "До следующего телепорта", 42, 102, 240) }
            }
    }

    private fun teleportAvailable(team: Team) {
        team.players.mapNotNull { Bukkit.getPlayer(it) }
            .forEach {
                Anime.killboardMessage(it, "Телепорт на чужие базы теперь §aдоступен")
                it.playSound(
                    it.location,
                    Sound.BLOCK_PORTAL_AMBIENT,
                    1.5f,
                    1.5f
                )
            }
    }
}