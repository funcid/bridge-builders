package me.reidj.node.game

import me.func.mod.conversation.ModTransfer
import me.func.mod.util.after
import me.reidj.bridgebuilders.error
import me.reidj.bridgebuilders.getUser
import me.reidj.node.activeStatus
import me.reidj.node.slots
import me.reidj.node.team.Team
import me.reidj.node.teams
import me.reidj.node.timer.Status
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartySnapshot
import java.util.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object InteractHandler : Listener {

    private val error = Formatting.error("Команда заполнена")

    @EventHandler
    fun PlayerItemHeldEvent.handle() {
        if (activeStatus != Status.STARTING)
            return
        val newItem = player.inventory.getItem(newSlot)
        if (newItem != player.inventory.getItem(previousSlot))
            after { showTeamList(player) }
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (activeStatus == Status.STARTING) {
            val uuid = player.uniqueId
            if (material == Material.WOOL) {
                teams.filter { !it.players.contains(uuid) && it.color.woolData.toByte() == player.itemInHand.getData().data }
                    .forEach { team ->
                        if (team.players.size >= slots / teams.size) {
                            player.error("Ошибка", error)
                            return@forEach
                        }
                        val party: Optional<*> = IPartyService.get().getPartyByMember(uuid).get()
                        if (party.isPresent) {
                            val partySnapshot = party.get() as PartySnapshot
                            if (partySnapshot.leader == uuid) {
                                if (team.players.size >= 1) {
                                    player.error("Ошибка", error)
                                    return@forEach
                                } else if (partySnapshot.members.size > 2) {
                                    player.error("Ошибка", "Максимальный размер тусовки 2 игрока!")
                                    return@forEach
                                }
                                partySnapshot.members.mapNotNull { Bukkit.getPlayer(it) }
                                    .forEach { partyMember -> commandChoose(team, partyMember) }
                            } else {
                                player.error("Ошибка", "Вы должны быть лидером тусовки, чтобы выбрать команду!")
                            }
                        } else {
                            commandChoose(team, player)
                        }
                    }
            }
            if (item == null)
                return
            val nmsItem = CraftItemStack.asNMSCopy(item)
            val tag = nmsItem.tag
            if (nmsItem.hasTag() && tag.hasKeyOfType("click", 8))
                player.performCommand(tag.getString("click"))
        }
    }

    private fun commandChoose(team: Team, player: Player) {
        val uuid = player.uniqueId
        val prevTeam = teams.firstOrNull { it.players.contains(uuid) }

        prevTeam?.players?.remove(uuid)
        team.players.add(uuid)

        // Удаляем у всех игрока из команды и добавляем в другую
        val prevTeamIndex = teams.indexOf(prevTeam)
        Bukkit.getOnlinePlayers().filter {
            it.inventory.heldItemSlot == prevTeamIndex || it.inventory.heldItemSlot == teams.indexOf(
                team
            )
        }.forEach { showTeamList(it) }
        player.sendMessage(Formatting.fine("Вы выбрали команду: " + team.color.chatFormat + team.color.teamName))
    }

    fun showTeamList(player: Player) {
        if (slots > 8)
            return

        val teamIndex = player.inventory.heldItemSlot
        val item = player.inventory.getItem(teamIndex)

        val template = ModTransfer(teamIndex)

        if (item != null && item.getType() == Material.WOOL) {
            val players = teams[teamIndex].players
            players.take(4).mapNotNull { getUser(it)?.cachedPlayer }.forEach {
                template.string(it.name)
            }
            repeat(4 - players.size) {
                template.string(if (it < slots / teams.size - players.size) " §7..." else "")
            }
        }
        template.send("bridge:team", player)
    }
}