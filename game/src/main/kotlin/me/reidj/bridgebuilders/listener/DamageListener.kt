package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.bukkit.Cycle
import me.func.mod.Anime
import me.reidj.bridgebuilders.Status
import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.donate.impl.Corpse
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.mod.ModHelper
import me.reidj.bridgebuilders.teams
import me.reidj.bridgebuilders.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.util.UtilEntity

object DamageListener : Listener {

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
            val victim = teams.filter { team -> team.players.contains(player.uniqueId) }
            val killer = teams.filter { team -> team.players.contains(player.killer.uniqueId) }
            drops.filter { it.getType().isBlock }.forEach { player.killer.inventory.addItem(it) }
            ModHelper.allNotification("" + victim[0].color.chatColor + player.name + "§f " + user.stat.activeKillMessage.getFormat() + " " + killer[0].color.chatColor + player.killer.name)
            if (user.stat.activeCorpse != Corpse.NONE) {
                val grave = StandHelper(location.clone().subtract(0.0, 3.6, 0.0))
                    .marker(true)
                    .invisible(true)
                    .gravity(false)
                    .slot(EnumItemSlot.HEAD, user.stat.activeCorpse.getIcon())
                    .markTrash()
                    .build()
                val name = StandHelper(location.clone().add(0.0, 1.0, 0.0))
                    .marker(true)
                    .invisible(true)
                    .gravity(false)
                    .name("§e${player.name}")
                    .build()
                UtilEntity.setScale(grave, 2.0, 2.0, 2.0)
                B.postpone(120 * 20) {
                    grave.remove()
                    name.remove()
                }
            }
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

        // Удаление вещей
        player.inventory
            .filterNotNull()
            .forEach { removeItems(it) }
        removeItems(player.itemOnCursor)
        removeItems(player.inventory.itemInOffHand)
        player.openInventory.topInventory.filterNotNull().forEach { removeItems(it) }

        player.updateInventory()

        player.gameMode = GameMode.SPECTATOR

        Cycle.run(20, 5) {
            if (it == 5) {
                player.gameMode = GameMode.SURVIVAL
                teams.stream()
                    .filter { team -> team.players.contains(player.uniqueId) }
                    .forEach { team ->
                        run {
                            val spawn = team.spawn
                            spawn.pitch = team.pitch
                            spawn.yaw = team.yaw
                            player.teleport(spawn)
                            player.foodLevel = 20
                        }
                    }
                Cycle.exit()
            } else if (it < 2) {
                Anime.title(player, "Возрождение...")
            } else if (it == 2) {
                Anime.counting321(player)
            }
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.handle() {
        teams.filter { team -> team.players.contains(damager.uniqueId) }
            .filter { it.players.contains(entity.uniqueId) }
            .forEach { _ -> isCancelled = true }
    }

    private fun removeItems(itemStack: ItemStack) {
        val toDelete = mutableListOf<ItemStack>()
        if (itemStack.getAmount() > 4) {
            itemStack.setAmount(itemStack.getAmount() - 2)
        } else {
            val name = itemStack.getType().name
            if (name.endsWith("SWORD") || name.endsWith("AXE")
                || name.endsWith("PICKAXE") || name.endsWith("CHESTPLATE") || name.endsWith("LEGGINGS")
                || name.endsWith("HELMET") || name.endsWith("BOOTS")
            ) {
                if (itemStack.getDurability() >= 0)
                    itemStack.setDurability((itemStack.getDurability() + 20).toShort())
                else
                    toDelete.add(itemStack)
            } else {
                toDelete.add(itemStack)
            }
        }
        toDelete.forEach { it.setAmount(0) }
        toDelete.clear()
    }
}