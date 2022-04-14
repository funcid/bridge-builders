package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.bukkit.Cycle
import me.func.mod.Anime
import me.reidj.bridgebuilders.Status
import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.donate.impl.Corpse
import me.reidj.bridgebuilders.donate.impl.KillMessage
import me.reidj.bridgebuilders.teams
import me.reidj.bridgebuilders.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
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
        val cause = player.lastDamageCause
        val victim = teams.filter { team -> team.players.contains(player.uniqueId) }[0]

        //BattlePassUtil.update(player.killer, QuestType.KILL, 1)

        if (cause.cause == EntityDamageEvent.DamageCause.FALL)
            printDeathMessage("Игрок ${victim.color.chatFormat + player.name} §fприземлился с большой высоты")
        else if (cause.cause == EntityDamageEvent.DamageCause.VOID)
            printDeathMessage("Игрока ${victim.color.chatFormat + player.name} §fпоглотила бездна")

        var location = player.location.clone()
        var id: Int
        var counter = 0
        do {
            counter++
            location = location.clone().subtract(0.0, 0.15, 0.0)
            id = location.block.typeId
        } while ((id == 0 || id == 171 || id == 96 || id == 167) && counter < 20)

        if (player.killer != null) {
            val killer = teams.filter { team -> team.players.contains(player.killer.uniqueId) }[0]
            // Удаление и выдача убийцы некоторых вещей
            drops.filter {
                it.getType().isBlock || it.getType() == Material.DIAMOND || it.getType() == Material.IRON_INGOT
                        || it.getType() == Material.COAL || it.getType() == Material.GOLD_INGOT
            }.forEach {
                    player.killer.inventory.addItem(it)
                    it.setAmount(0)
                }
            // Сообщение об убийстве
            app.getUser(player)?.let { user ->
                Bukkit.getOnlinePlayers().forEach {
                    Anime.killboardMessage(
                        it,
                        "" + victim.color.chatColor + player.name + "§f " + KillMessage.valueOf(user.stat.activeKillMessage.name).getFormat() + " игроком " + killer.color.chatColor + player.killer.name
                    )
                }
                // Создаю гроб, лол
                if (user.stat.activeCorpse != data.Corpse.NONE) {
                    val grave = StandHelper(location.clone().subtract(0.0, 3.6, 0.0))
                        .marker(true)
                        .invisible(true)
                        .gravity(false)
                        .slot(EnumItemSlot.HEAD, Corpse.valueOf(user.stat.activeCorpse.name).getIcon())
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
            // Начисление убийце статистики
            app.getUser(player.killer)?.let { killerStats ->
                killerStats.giveMoney(3)
                killerStats.stat.kills++
                killerStats.kills++
                killerStats.player!!.sendMessage(Formatting.fine("Вы получили §e3 монеты §fза убийство."))
            }
        }

        teams.filter { it.players.contains(player.uniqueId) }.forEach { team ->
            team.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
                player.playSound(
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

        Cycle.run(20, 5) { time ->
            if (time == 5) {
                player.gameMode = GameMode.SURVIVAL
                if (teams.none { it.players.contains(player.uniqueId) })
                    return@run
                val team = teams.filter { it.players.contains(player.uniqueId) }[0]
                run {
                    app.teleportAtBase(team, player)
                    player.foodLevel = 20
                }
                Cycle.exit()
            } else if (time < 2) {
                Anime.title(player, "Возрождение...")
            } else if (time == 2) {
                Anime.counting321(player)
            }
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.handle() {
        // Отключение урона по союзникам
        teams.filter { team -> team.players.contains(damager.uniqueId) }
            .filter { it.players.contains(entity.uniqueId) }
            .forEach { _ -> isCancelled = true }
        if (damager is Player && (damager as Player).itemInHand.getType().name.endsWith("AXE"))
            damage /= 3
    }

    private fun removeItems(itemStack: ItemStack) {
        val toDelete = mutableListOf<ItemStack>()
        if (itemStack.getAmount() > 4) {
            itemStack.setAmount(itemStack.getAmount() - 2)
        } else {
            val name = itemStack.getType().name
            if (name.endsWith("SWORD") || name.endsWith("AXE") || name.endsWith("PICKAXE") || name.endsWith("SPADE")
                || name.endsWith("CHESTPLATE") || name.endsWith("LEGGINGS") || name.endsWith("HELMET") || name.endsWith(
                    "BOOTS"
                )
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

    private fun printDeathMessage(text: String) = Bukkit.getOnlinePlayers().forEach { Anime.killboardMessage(it, text) }
}