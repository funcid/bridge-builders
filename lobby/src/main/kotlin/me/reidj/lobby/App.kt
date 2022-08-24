package me.reidj.lobby

import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import kotlinx.coroutines.runBlocking
import me.func.mod.Anime
import me.func.mod.Kit
import me.func.mod.conversation.ModLoader
import me.func.mod.util.listener
import me.reidj.bridgebuilders.bulkSave
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.plugin
import me.reidj.bridgebuilders.util.MapLoader
import me.reidj.bridgebuilders.worldMeta
import me.reidj.lobby.command.AdminCommands
import me.reidj.lobby.command.PlayerCommands
import me.reidj.lobby.content.Customization
import me.reidj.lobby.content.LootBox
import me.reidj.lobby.listener.ConnectionHandler
import me.reidj.lobby.listener.InteractHandler
import me.reidj.lobby.listener.UnusedHandler
import me.reidj.lobby.npc.NpcManager
import me.reidj.lobby.ticker.detail.BanManager
import me.reidj.lobby.ticker.detail.RepeatTasks
import me.reidj.lobby.ticker.detail.TopManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

const val SLOTS = 1000

lateinit var app: App

class App : JavaPlugin() {

    private lateinit var npcManager: NpcManager

    lateinit var lootBox: LootBox

    override fun onEnable() {
        app = this
        plugin = this

        Platforms.set(PlatformDarkPaper())

        CoreApi.get().run {
            registerService(IPartyService::class.java, PartyService(clientSocket))
            registerService(ITransferService::class.java, TransferService(clientSocket))
            registerService(IInventoryService::class.java, InventoryService())
        }

        Anime.include(Kit.NPC, Kit.STANDARD, Kit.LOOTBOX, Kit.GRAFFITI, Kit.EXPERIMENTAL, Kit.BATTLEPASS)

        // Mods
        ModLoader.loadAll("mods")

        // Конфигурация реалма
        IRealmService.get().currentRealmInfo.run {
            status = RealmStatus.WAITING_FOR_PLAYERS
            maxPlayers = SLOTS
            isLobbyServer = true
            readableName = "BridgeBuildersLobby"
            groupName = "BridgeBuildersLobby"
            servicedServers = arrayOf("BRD")
        }

        worldMeta = MapLoader().load("LOBB2")

        lootBox = LootBox()
        npcManager = NpcManager()
        Customization()

        // Регистрация команд
        AdminCommands()
        PlayerCommands()

        listener(GlobalListeners(), npcManager, lootBox, ConnectionHandler(), InteractHandler(), UnusedHandler())

        // Обновление топа
        Bukkit.getServer().scheduler.runTaskTimerAsynchronously(this, TopManager(), 0, 60 * 20)

        // Обработка каждого тика
        TickTimerHandler(npcManager, lootBox, BanManager(), RepeatTasks()).runTaskTimerAsynchronously(this, 0, 1)
    }

    override fun onDisable() {
        runBlocking { clientSocket.write(bulkSave(true)) }
        Thread.sleep(1000)
    }

    fun getHub(): RealmId = RealmId.of("HUB-2")
}