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
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.util.StandHelper
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
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
        if (entity !is Player)
            return
        if (activeStatus != Status.GAME || (entity as Player).gameMode == GameMode.SPECTATOR)
            cancelled = true
    }

    @EventHandler
    fun PlayerDeathEvent.handle() {
        cancelled = true

        val cause = getEntity().lastDamageCause
        val victim = teams.filter { team -> team.players.contains(getEntity().uniqueId) }[0]

        val user = app.getUser(getEntity())!!

        if (cause.cause == EntityDamageEvent.DamageCause.FALL)
            printDeathMessage("Игрок ${victim.color.chatFormat + getEntity().name} §fприземлился с большой высоты")
        else if (cause.cause == EntityDamageEvent.DamageCause.VOID && user.lastDamager == null)
            printDeathMessage("Игрока ${victim.color.chatFormat + getEntity().name} §fпоглотила бездна")

        var location = getEntity().location.clone()
        var id: Int
        var counter = 0
        do {
            counter++
            location = location.clone().subtract(0.0, 0.15, 0.0)
            id = location.block.typeId
        } while ((id == 0 || id == 171 || id == 96 || id == 167) && counter < 20)

        // Удаление вещей
        getEntity().inventory.filterNotNull().forEach { removeItems(user, it) }
        removeItems(user, getEntity().itemOnCursor)
        getEntity().openInventory.topInventory.filterNotNull().forEach { removeItems(user, it) }

        getEntity().updateInventory()

        getEntity().gameMode = GameMode.SPECTATOR

        Cycle.run(20, 5) { time ->
            if (time == 5) {
                getEntity().gameMode = GameMode.SURVIVAL
                if (teams.none { it.players.contains(getEntity().uniqueId) })
                    return@run
                val team = teams.filter { it.players.contains(getEntity().uniqueId) }[0]
                run {
                    app.teleportAtBase(team, getEntity())
                    getEntity().foodLevel = 20
                    user.lastDamager = null
                }
                Cycle.exit()
            } else if (time < 2) {
                Anime.title(getEntity(), "Возрождение...")
            } else if (time == 2) {
                Anime.counting321(getEntity())
            }
        }

        if (user.lastDamager != null) {
            val killer = teams.filter { team -> team.players.contains(user.lastDamager?.uniqueId) }
            val killerStatistic = app.getUser(user.lastDamager!!)

            if (killer.isEmpty()) {
                user.lastDamager = null
                return
            }
            // Сообщение об убийстве
            Bukkit.getOnlinePlayers().forEach {
                Anime.killboardMessage(
                    it,
                    "" + victim.color.chatColor + getEntity().name + "§f " + killerStatistic?.stat?.activeKillMessage?.name?.let { it1 ->
                        KillMessage.valueOf(it1)
                            .getFormat()
                    } + " игроком " + killer[0].color.chatColor + user.lastDamager!!.name
                )
            }
            // Начисление убийце статистики
            killerStatistic?.let {
                it.giveMoney(5)
                it.stat.kills++
                it.kills++
                it.player!!.sendMessage(Formatting.fine("Вы получили §e5 монет §fза убийство."))
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
                    .name("§e${getEntity().name}")
                    .build()
                UtilEntity.setScale(grave, 2.0, 2.0, 2.0)
                B.postpone(120 * 20) {
                    grave.remove()
                    name.remove()
                }
            }
        }

        teams.filter { it.players.contains(getEntity().uniqueId) }.forEach { team ->
            team.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
                player.playSound(
                    player.location,
                    org.bukkit.Sound.ENTITY_ENDERDRAGON_AMBIENT,
                    1f,
                    1f
                )
            }
        }
    }

    @EventHandler
    fun EntityDamageByEntityEvent.handle() {
        if (activeStatus == Status.STARTING)
            return
        // Отключение урона по союзникам
        if ((damager is Player || damager is Arrow) && entity is Player) {
            val damager = if (damager is Projectile) (damager as Projectile).shooter as Player else damager as Player
            if (!teams.filter { it.players.contains(damager.uniqueId) }[0].players.contains(entity.uniqueId))
                app.getUser(entity as Player)!!.lastDamager = damager
            if (teams.any { team -> team.players.contains(damager.uniqueId) } && teams.filter { team ->
                    team.players.contains(damager.uniqueId)
                }[0].players.contains(entity.uniqueId))
                isCancelled = true
            if (damager.itemInHand.getType().name.endsWith("AXE"))
                damage /= 3
        }
    }

    private val lapis = ItemStack(Material.INK_SACK, 1, 4.toShort())

    fun removeItems(entity: User, itemStack: ItemStack) {
        val type = itemStack.getType()
        if (itemStack.getAmount() >= 4) {
            var amount = itemStack.getAmount()
            itemStack.setAmount(itemStack.getAmount() - 2)
            amount -= itemStack.getAmount()
            if (entity.lastDamager != null) {
                entity.lastDamager!!.inventory!!.addItem(ItemStack(itemStack.getType(), amount))
                if (type.isBlock || type == Material.DIAMOND || type == Material.IRON_INGOT || type == Material.COAL || type == Material.GOLD_INGOT || itemStack == lapis)
                    itemStack.setAmount(0)
            }
        }
    }

    private fun printDeathMessage(text: String) =
        Bukkit.getOnlinePlayers().forEach { Anime.killboardMessage(it, text) }
}