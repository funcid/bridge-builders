package me.reidj.node

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import kotlinx.coroutines.runBlocking
import me.func.mod.Anime
import me.func.mod.Kit
import me.func.mod.conversation.ModLoader
import me.func.mod.util.after
import me.func.mod.util.listener
import me.reidj.bridgebuilders.bulkSave
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.plugin
import me.reidj.bridgebuilders.protocol.RejoinPackage
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import me.reidj.node.command.AdminCommands
import me.reidj.node.command.PlayerCommands
import me.reidj.node.game.BridgeGame
import me.reidj.node.team.Team
import me.reidj.node.timer.Status
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.datasync.EntityDataParameters
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.Capability
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmInfo
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

lateinit var app: App
lateinit var teams: MutableList<Team>
lateinit var realm: RealmInfo

var slots = 8

var activeStatus: Status = Status.STARTING

class App : JavaPlugin() {

    override fun onEnable() {
        app = this
        plugin = this
        B.plugin = this

        Platforms.set(PlatformDarkPaper())
        EntityDataParameters.register()

        clientSocket.registerCapability(
            Capability.builder()
                .className(RejoinPackage::class.java.name)
                .notification(true)
                .build()
        )

        clientSocket.addListener(RejoinPackage::class.java) { _, pckg ->
            val uuid = pckg.uuid
            (getUser(uuid) ?: return@addListener).stat.run {
                gameExitTime = -1
                lastRealm = ""
                after(100) {
                    clientSocket.write(SaveUserPackage(uuid, this))
                }
            }
        }

        CoreApi.get().run {
            registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
            registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
            registerService(IInventoryService::class.java, InventoryService())
        }

        // Конфигурация реалма
        realm = IRealmService.get().currentRealmInfo.apply {
            val id = realmId.id
            status = RealmStatus.WAITING_FOR_PLAYERS
            maxPlayers = 32
            lobbyFallback = RealmId.of("BRIL-1")
            readableName = "BridgeBuilders#$id"
            groupName = "BridgeBuilders#$id"
            isCanReconnect = true
        }

        Anime.include(Kit.EXPERIMENTAL, Kit.HEALTH_BAR, Kit.NPC, Kit.STANDARD, Kit.GRAFFITI)

        // Mods
        ModLoader.loadAll("mods")

        listener(GlobalListeners())

        // Регистрация команд
        AdminCommands()
        PlayerCommands()

        BridgeGame()
    }

    override fun onDisable() {
        runBlocking { clientSocket.write(bulkSave(true)) }
        Thread.sleep(1000)
    }
}