package me.reidj.bridgebuilders

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import dev.implario.bukkit.item.item
import me.func.mod.Banners
import me.func.mod.Npc
import me.func.mod.conversation.ModLoader
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.realm.RealmId

object LobbyHandler : Listener {

    private var cosmeticItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§aПерсонаж")
        nbt("other", "clothes")
        nbt("click", "menu")
    }.build()
    private var startItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§bИграть")
        nbt("other", "guild_members")
        nbt("click", "next")
    }.build()
    private var backItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§cВыйти")
        nbt("other", "cancel")
        nbt("click", "leave")
    }.build()

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (item == null)
            return
        val nmsItem = CraftItemStack.asNMSCopy(item)
        if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("click", 8))
            player.performCommand(nmsItem.tag.getString("click"))
    }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (player.location.block.y <= 2)
            player.teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5))
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.handle() = Npc.npcs[entityId]?.click!!.accept(this)

    @EventHandler
    fun PlayerJoinEvent.handle() = player.apply {
        val user = app.getUser(this)

        if (user == null) {
            sendMessage(Formatting.error("Нам не удалось прогрузить Вашу статистику."))
            B.postpone(3) {
                Cristalix.transfer(
                    setOf(uniqueId),
                    RealmId.of(HUB)
                )
            }
        }

        allowFlight = IPermissionService.get().isDonator(uniqueId)
        ModLoader.send("balance-bundle.jar", this)
        B.postpone(5) {
            teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5))
            Npc.npcs.values.forEach { it.spawn(this) }
            Banners.banners.values.forEach { Banners.show(this, it) }
        }
        user!!.player = this
        user.giveMoney(0)
    }

    @EventHandler
    fun PlayerSpawnLocationEvent.handle() {
        player.inventory.setItem(0, startItem)
        player.inventory.setItem(4, cosmeticItem)
        player.inventory.setItem(8, backItem)
    }

    @EventHandler
    fun EntityDamageEvent.handle() = apply { isCancelled = true }

    @EventHandler
    fun BlockPhysicsEvent.handle() = apply { cancel = true }

    @EventHandler
    fun FoodLevelChangeEvent.handle() = apply { level = 20 }

    @EventHandler
    fun BlockBreakEvent.handle() = apply { cancel = true }

    @EventHandler
    fun PlayerDropItemEvent.handle() = apply { cancel = true }

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        isCancelled = true
        Bukkit.getOnlinePlayers().forEach { it.sendMessage(getPrefix(app.getUser(player)!!, false) + message) }
    }
}