package me.reidj.bridgebuilders.listener

import clepto.bukkit.Cycle
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.donate.impl.Corpse
import me.reidj.bridgebuilders.mod.ModHelper
import me.reidj.bridgebuilders.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack

object DamageListener : Listener {

    private var toDelete: MutableList<ItemStack> = mutableListOf()

    @EventHandler
    fun EntityDamageEvent.handle() {
        if (activeStatus != Status.GAME || (entity as Player).gameMode == GameMode.SPECTATOR)
            cancelled = true
    }

    @EventHandler
    fun PlayerDeathEvent.handle() {
        cancelled = true

        val player = entity as Player

        var location = player.location.clone()
        var id: Int
        var counter = 0
        do {
            counter++
            location = location.clone().subtract(0.0, 0.15, 0.0)
            id = location.block.typeId
        } while ((id == 0 || id == 171 || id == 96 || id == 167) && counter < 20)

        if (player.killer != null) {
            val user = getByPlayer(player)
            teams.forEach {
                ModHelper.allNotification("" + it.color.chatColor + player.name + "§f" + user.stat.activeKillMessage.getFormat() + " " + it.color.chatColor + player.killer.name)
            }
            if (user.stat.activeCorpse != Corpse.NONE)
                StandHelper(location.clone().subtract(0.0, 1.5, 0.0))
                    .marker(true)
                    .invisible(true)
                    .gravity(false)
                    .slot(EnumItemSlot.HEAD, user.stat.activeCorpse.getIcon())
                    .markTrash()
        }

        teams.filter { it.players.contains(player.uniqueId) }.forEach {
            it.players.forEach { uuid ->
                Bukkit.getPlayer(uuid).playSound(
                    player.location,
                    org.bukkit.Sound.ENTITY_ENDERDRAGON_AMBIENT,
                    1f,
                    1f
                )
            }
        }

        if (player.gameMode == GameMode.SPECTATOR)
            return

        player.inventory
            .filterNotNull()
            .forEach {
                if (it.getAmount() > 4) {
                    it.setAmount(it.getAmount() - 2)
                } else {
                    val name = it.getType().name
                    if (name.endsWith("SWORD") || name.endsWith("AXE") || name.endsWith("PICKAXE")) {
                        if (it.getDurability() >= 0)
                            it.setDurability((it.getDurability() + 20).toShort())
                        else
                            toDelete.add(it)
                    } else {
                        toDelete.add(it)
                    }
                }
            }
        toDelete.forEach { player.inventory.remove(it) }
        toDelete.clear()

        player.gameMode = GameMode.SPECTATOR

        Cycle.run(20, 5) {
            if (it == 5) {
                player.gameMode = GameMode.SURVIVAL
                teams.stream()
                    .filter { team -> team.players.contains(player.uniqueId) }
                    .forEach { team -> player.teleport(team.spawn) }
                Cycle.exit()
            }
            ModHelper.sendTitle(app.getUser(player), "До возрождения ${5 - it}")
        }
    }
}