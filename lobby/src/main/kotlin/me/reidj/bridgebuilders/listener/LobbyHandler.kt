package me.reidj.bridgebuilders.listener

import clepto.cristalix.Cristalix
import dev.implario.bukkit.item.item
import me.func.mod.Anime
import me.func.mod.Npc.skin
import me.func.mod.selection.Confirmation
import me.func.mod.selection.Reconnect
import me.func.mod.util.after
import me.func.protocol.Indicators
import me.reidj.bridgebuilders.HUB
import me.reidj.bridgebuilders.app
import me.reidj.bridgebuilders.getPrefix
import me.reidj.bridgebuilders.npc.NpcManager
import me.reidj.bridgebuilders.npc.NpcType
import me.reidj.bridgebuilders.reward.WeekRewards
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus

object LobbyHandler : Listener {

    private var gameItem = item {
        type = Material.CLAY_BALL
        text("§aИграть")
        nbt("other", "guild_members")
        nbt("click", "game")
    }.build()
    private var cosmeticItem = item {
        type = Material.CLAY_BALL
        text("§aПерсонаж")
        nbt("other", "clothes")
        nbt("click", "menu")
    }.build()
    private var backItem = item {
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

    private val spawn = worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5).apply { yaw = 90f }

    @EventHandler
    fun PlayerMoveEvent.handle() {
        if (player.location.block.y <= 2)
            player.teleport(spawn)
    }

    private val reconnect = Reconnect(300) { it.performCommand("rejoin") }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        after(10) {
            val user = app.getUser(player)

            if (user == null) {
                player.sendMessage(Formatting.error("Нам не удалось загрузить Вашу статистику."))
                Cristalix.transfer(listOf(player.uniqueId), RealmId.of(HUB))
                return@after
            }

            val stat = user.stat

            player.allowFlight = IPermissionService.get().isDonator(player.uniqueId)
            user.player = player

            player.teleport(spawn)

            Anime.hideIndicator(player, Indicators.ARMOR, Indicators.EXP, Indicators.HEALTH, Indicators.HUNGER)

            NpcManager.npcs[NpcType.GUIDE.name]!!.first.data.skin(player.uniqueId.toString())

            user.giveMoney(0, true)

            if (!user.stat.isApprovedResourcepack)
                confirmation.open(player)
            else
                player.performCommand("resourcepack")

            if (IRealmService.get()
                    .getRealmById(RealmId.of(user.stat.realm)) != null && (user.stat.realm != "" || IRealmService.get()
                    .getRealmById(RealmId.of(user.stat.realm)).status != RealmStatus.WAITING_FOR_PLAYERS) && !user.stat.isBan
            ) {
                reconnect.text = "Вернуться в игру"
                reconnect.hint = "Вернуться"
                reconnect.open(player)
            }

            val now = System.currentTimeMillis()
            // Обнулить комбо сбора наград если прошло больше суток или комбо > 7
            if ((stat.rewardStreak > 0 && now - stat.lastEnterTime * 10000 > 24 * 60 * 60 * 1000) || stat.rewardStreak > 6) {
                stat.rewardStreak = 0
            }
            if (now - stat.dailyTimestamp * 10000 > 14 * 60 * 60 * 1000) {
                Anime.close(player)
                stat.dailyTimestamp = now.toDouble() / 10000
                Anime.openDailyRewardMenu(
                    player,
                    stat.rewardStreak,
                    *WeekRewards.values().map { it.reward }.toTypedArray()
                )

                val dailyReward = WeekRewards.values()[stat.rewardStreak]
                player.sendMessage(Formatting.fine("Ваша ежедневная награда: " + dailyReward.reward.title))
                dailyReward.give(user)
                stat.rewardStreak++
            }
            stat.lastEnterTime = now.toDouble() / 10000
        }
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
    fun BlockPlaceEvent.handle() = apply { cancel = true }

    @EventHandler
    fun PlayerPickupItemEvent.handle() { isCancelled = true }

    @EventHandler
    fun PlayerDropItemEvent.handle() = apply { cancel = true }

    @EventHandler
    fun AsyncPlayerChatEvent.handle() {
        isCancelled = true
        Bukkit.getOnlinePlayers().forEach { it.sendMessage(getPrefix(app.getUser(player)!!, false) + message) }
    }
}