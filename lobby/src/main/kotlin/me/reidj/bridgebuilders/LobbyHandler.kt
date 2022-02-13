package me.reidj.bridgebuilders

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.mod.Npc
import me.func.mod.Npc.location
import me.func.mod.Npc.onClick
import me.func.protocol.npc.NpcBehaviour
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import ru.cristalix.core.permissions.IPermissionService

object LobbyHandler : Listener {

    private var cosmeticItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§aПерсонаж")
        nbt("other", "clothes")
        nbt("click", "menu")
    }
    private var startItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§bИграть")
        nbt("other", "guild_members")
        nbt("click", "next")
    }
    private var backItem: ItemStack = item {
        type = Material.CLAY_BALL
        text("§cВыйти")
        nbt("other", "cancel")
        nbt("click", "leave")
    }

    @EventHandler
    fun PlayerInteractEvent.handle() {
        if (item == null)
            return
        val nmsItem = CraftItemStack.asNMSCopy(item)
        if (nmsItem.hasTag() && nmsItem.tag.hasKeyOfType("click", 8))
            player.performCommand(nmsItem.tag.getString("click"))
    }

    private val balancer = PlayerBalancer()
    private var fixDoubleClick: Player? = null

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.allowFlight = IPermissionService.get().isDonator(player.uniqueId)

        // NPC поиска игры
        B.postpone(5) {
            worldMeta.getLabels("play").forEach { npcLabel ->
                val npcArgs = npcLabel.tag.split(" ")
                Npc.npc {
                    onClick {
                        val player = it.player
                        if (fixDoubleClick != null && fixDoubleClick == player)
                            return@onClick
                        balancer.accept(player)
                        fixDoubleClick = player
                    }
                    name = "§e§lНАЖМИТЕ ЧТОБЫ ИГРАТЬ"
                    behaviour = NpcBehaviour.STARE_AT_PLAYER
                    skinUrl = "https://webdata.c7x.dev/textures/skin/$SKIN"
                    skinDigest = SKIN
                    location(npcLabel.clone().add(0.5, 0.0, 0.5))
                    yaw = npcArgs[0].toFloat()
                    pitch = npcArgs[1].toFloat()
                }.spawn(player)
            }
            // Создание NPC
            val npcLabel = worldMeta.getLabel("guide")
            val npcArgs = npcLabel.tag.split(" ")
            Npc.npc {
                onClick { it.player.performCommand("menu") }
                location(npcLabel.clone().add(0.5, 0.0, 0.5))
                name = "§dПерсонализация"
                behaviour = NpcBehaviour.STARE_AT_PLAYER
                skinUrl = "https://webdata.c7x.dev/textures/skin/$SKIN"
                skinDigest = SKIN
                yaw = npcArgs[0].toFloat()
                pitch = npcArgs[1].toFloat()
            }.spawn(player)
        }
    }

    @EventHandler
    fun PlayerSpawnLocationEvent.handle() {
        player.inventory.setItem(0, startItem)
        player.inventory.setItem(4, cosmeticItem)
        player.inventory.setItem(8, backItem)
    }

    @EventHandler
    fun EntityDamageEvent.handle() {
        cancelled = true
    }

    @EventHandler
    fun BlockPhysicsEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun FoodLevelChangeEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockBreakEvent.handle() {
        cancel = true
    }

    @EventHandler
    fun PlayerDropItemEvent.handle() {
        cancel = true
    }
}