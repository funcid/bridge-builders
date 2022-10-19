package me.reidj.node.game

import clepto.bukkit.Cycle
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.protocol.GlowColor
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.donate.impl.GraveType
import me.reidj.bridgebuilders.donate.impl.MessageType
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.isSpectator
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import me.reidj.bridgebuilders.user.User
import me.reidj.node.activeStatus
import me.reidj.node.teams
import me.reidj.node.timer.Status
import me.reidj.node.util.StandHelper
import me.reidj.node.util.after
import net.minecraft.server.v1_12_R1.EnumItemSlot
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.util.UtilEntity

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class DamageHandler : Listener {

    @EventHandler
    fun EntityDamageEvent.handle() {
        if (entity !is Player)
            return
        isCancelled = activeStatus != Status.GAME || (entity as Player).isSpectator()
    }

    @EventHandler
    fun PlayerDeathEvent.handle() {
        isCancelled = true

        val cause = getEntity().lastDamageCause.cause
        val uuid = getEntity().uniqueId
        val inventory = getEntity().inventory
        val team = teams.firstOrNull { uuid in it.players } ?: return
        val user = getUser(uuid) ?: return
        val chatColor = team.color.chatColor

        val entityDeathMessage = MessageType.valueOf(user.stat.currentMessages).messages[2]
            .replace("%e", "$chatColor${getEntity().name}")

        if (cause == EntityDamageEvent.DamageCause.FALL)
            printDeathMessage(entityDeathMessage)
        else if (cause == EntityDamageEvent.DamageCause.VOID && user.lastDamager == null)
            printDeathMessage(entityDeathMessage)

        var location = getEntity().location.clone()
        var id: Int
        var counter = 0
        do {
            counter++
            location = location.clone().subtract(0.0, 0.15, 0.0)
            id = location.block.typeId
        } while ((id == 0 || id == 171 || id == 96 || id == 167) && counter < 20)

        if (user.stat.currentGrave != GraveType.NONE.name) {
            val grave = StandHelper(location.clone().subtract(0.0, 3.6, 0.0))
                .marker(true)
                .invisible(true)
                .gravity(false)
                .slot(EnumItemSlot.HEAD, GraveType.valueOf(user.stat.currentGrave).getIcon())
                .markTrash()
                .build()
            val graveName = StandHelper(location.clone().add(0.0, 1.0, 0.0))
                .marker(true)
                .invisible(true)
                .gravity(false)
                .name("§e${getEntity().name}")
                .build()
            UtilEntity.setScale(grave, 2.0, 2.0, 2.0)
            after(20 * 120) {
                grave.remove()
                graveName.remove()
            }
        }

        // Удаление вещей
        removeItems(
            user,
            inventory.itemInOffHand,
            getEntity().itemOnCursor,
            *inventory.mapNotNull { it }.toTypedArray(),
            *getEntity().openInventory.topInventory.mapNotNull { it }.toTypedArray()
        )

        getEntity().gameMode = GameMode.SPECTATOR

        Cycle.run(20, 5) { time ->
            if (time == 5) {
                Cycle.exit()
                team.baseTeleport(getEntity())
                getEntity().gameMode = GameMode.SURVIVAL
                getEntity().foodLevel = 20
                user.lastDamager = null
                user.isGod = true
                after(3 * 20) { user.isGod = false }
            } else if (time < 2) {
                Anime.title(getEntity(), "§dВозрождение...")
            } else if (time == 2) {
                Anime.counting321(getEntity())
            }
        }

        if (user.lastDamager != null) {
            val killer = getUser(user.lastDamager!!) ?: return
            val killerTeam = teams.firstOrNull { killer.stat.uuid in it.players } ?: return
            val killerChatColor = killerTeam.color.chatColor
            val message = "Вы получили §d5 эфира §fза убийство."

            printKillMessages(0, killer, user, chatColor, killerChatColor)
            printKillMessages(1, killer, user, chatColor, killerChatColor)

            killer.run {
                givePureEther(5)
                stat.kills++
                cacheKills++
                cachedPlayer?.let {
                    it.sendMessage(Formatting.fine(message))
                    Anime.killboardMessage(it, message)
                    Glow.animate(it, 0.5, GlowColor.BLUE)
                }
                clientSocket.write(SaveUserPackage(uuid, stat))
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun EntityDamageByEntityEvent.handle() {
        if (activeStatus == Status.STARTING)
            return
        // Отключение урона по союзникам
        if ((damager is Player || damager is Arrow) && entity is Player) {
            val damager = if (damager is Projectile) (damager as Projectile).shooter as Player else damager as Player
            val team = teams.firstOrNull { damager.uniqueId in it.players } ?: return
            val user = getUser(entity.uniqueId) ?: return
            if (entity.uniqueId in team.players || user.isGod) {
                cancelled = true
            } else {
                if (damager.itemInHand.getType().name.endsWith("AXE"))
                    damage /= 3
                user.lastDamager = damager
            }
        }
    }

    companion object {
        fun removeItems(entity: User, vararg itemStack: ItemStack) {
            itemStack.forEach {
                if (it.getAmount() >= 4) {
                    val prevAmount = it.getAmount()
                    it.setAmount(it.getAmount() - 2)
                    if (entity.lastDamager != null) {
                        val newItemStack = it.clone()
                        newItemStack.setAmount(prevAmount - it.getAmount())
                        entity.lastDamager!!.inventory.addItem(newItemStack)
                    }
                }
            }
        }
    }

    private fun printDeathMessage(text: String) =
        Bukkit.getOnlinePlayers().forEach { Anime.killboardMessage(it, text) }

    private fun printKillMessages(
        index: Int,
        killer: User,
        entity: User,
        chatColor: net.md_5.bungee.api.ChatColor,
        killerChatColor: net.md_5.bungee.api.ChatColor
    ) {
        val message = MessageType.valueOf(killer.stat.currentMessages).messages[index]
            .replace("%e", "$chatColor${entity.cachedPlayer?.name}§f")
            .replace("%k", "$killerChatColor${killer.cachedPlayer?.name}§f")

        Bukkit.broadcastMessage(message)
        Bukkit.getOnlinePlayers().forEach { Anime.killboardMessage(it, message) }
    }
}