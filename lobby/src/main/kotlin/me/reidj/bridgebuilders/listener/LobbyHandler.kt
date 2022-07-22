package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.item.item
import me.func.mod.conversation.ModLoader
import me.func.mod.selection.Confirmation
import me.func.mod.selection.Reconnect
import me.reidj.bridgebuilders.HUB
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.getPrefix
import me.reidj.bridgebuilders.worldMeta
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
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus

object LobbyHandler : Listener {

    private var gameItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§aИграть")
        nbt("other", "guild_members")
        nbt("click", "game")
    }.build()
    private var cosmeticItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§aПерсонаж")
        nbt("other", "clothes")
        nbt("click", "menu")
    }.build()
    private var backItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§cВыйти")
        nbt("other", "cancel")
        nbt("click", "leave")
    }.build()

    private val confirmation =
        Confirmation("Рекомендуем установить", "ресурспак") { send -> send.performCommand("resourcepack") }

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
    fun PlayerJoinEvent.handle() = player.apply {
        val user = app.getUser(this)

        if (user == null) {
            sendMessage(Formatting.error("Нам не удалось прогрузить Вашу статистику."))
            B.postpone(20) {
                Cristalix.transfer(
                    listOf(uniqueId),
                    RealmId.of(HUB)
                )
            }
        }

        user!!.player = this
        user.giveMoney(0)

        ModLoader.send("balance-bundle-1.0-SNAPSHOT.jar", this)
        allowFlight = IPermissionService.get().isDonator(uniqueId)

        B.postpone(5) {
            teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5))

            if (user.stat.isApprovedResourcepack)
                confirmation.open(this)
        }

        if (IRealmService.get().getRealmById(RealmId.of(user.stat.realm)) != null && (user.stat.realm != ""
                    || IRealmService.get().getRealmById(RealmId.of(user.stat.realm)).status != RealmStatus.WAITING_FOR_PLAYERS))
            Reconnect("Вернуться в игру", 60, "Вернуться") { player -> player.performCommand("/rejoin") }
    }

    @EventHandler
    fun PlayerSpawnLocationEvent.handle() {
        player.inventory.setItem(0, gameItem)
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