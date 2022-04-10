package me.reidj.bridgebuilders

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Anime
import me.func.mod.Kit
import me.reidj.bridgebuilders.content.CustomizationNPC
import me.reidj.bridgebuilders.content.Lootbox
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.util.MapLoader
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.render.BukkitRenderService
import ru.cristalix.core.render.IRenderService
import java.util.*

lateinit var app: App
const val SKIN: String = "bf30a1df-85de-11e8-a6de-1cb72caa35fd"

class App : JavaPlugin() {

    var online = 0

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())

        Anime.include(Kit.NPC, Kit.LOOTBOX, Kit.EXPERIMENTAL)

        BridgeBuildersInstance(this, { getUser(it) }, { getUser(it) }, MapLoader().load("LOBB"), 200)

        CoreApi.get().registerService(IRenderService::class.java, BukkitRenderService(getServer()))

        // Конфигурация реалма
        realm.status = RealmStatus.WAITING_FOR_PLAYERS
        realm.maxPlayers = 1200
        realm.isLobbyServer = true
        realm.readableName = "BridgeBuilders Lobby"
        realm.groupName = "BridgeBuilders Lobby"
        realm.servicedServers = arrayOf("BRI")

        // Создание контента для лобби
        TopManager()
        CustomizationNPC

        B.events(
            Lootbox,
            GlobalListeners,
            LobbyHandler
        )

        val npcLabel = worldMeta.getLabel("play")
        val stand = worldMeta.world.spawnEntity(
            npcLabel.clone().add(0.5, 2.3, 0.5),
            EntityType.ARMOR_STAND
        ) as ArmorStand
        stand.isMarker = true
        stand.isVisible = false
        stand.setGravity(false)
        stand.isCustomNameVisible = true
        B.repeat(20) {
            realm.servicedServers.forEach { online = IRealmService.get().getOnlineOnRealms(it) }
            stand.customName = "§bОнлайн $online"
        }

        // Команда выхода в хаб
        B.regCommand({ player, _ ->
            Cristalix.transfer(listOf(player.uniqueId), RealmId.of(HUB))
            null
        }, "leave")

        /*B.regCommand({ player, _ ->
            BattlePassManager.show(player)
            null
        }, "battlepass", "bp")*/
        B.regCommand({ player, args ->
            val realmId =
                IRealmService.get().getRealmsOfType("BRI")
                    .filter { it.status == RealmStatus.GAME_STARTED_CAN_SPACTATE }
                    .map { it.realmId }
            val realm = RealmId.of("BRI-${args[0]}")
            if (realmId.contains(realm))
                Cristalix.transfer(mutableListOf(player.uniqueId), realm)
            else
                player.sendMessage(Formatting.error("Сервер не найден."))
            null
        }, "spectate", "spec")
    }

    private fun getUser(player: Player) = userManager.getUser(player.uniqueId)

    private fun getUser(uuid: UUID) = userManager.getUser(uuid)
}