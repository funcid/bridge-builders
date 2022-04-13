package me.reidj.bridgebuilders

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import clepto.cristalix.WorldMeta
import com.google.gson.GsonBuilder
import dev.implario.kensuke.Kensuke
import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.Scope
import dev.implario.kensuke.impl.bukkit.BukkitKensuke
import dev.implario.kensuke.impl.bukkit.BukkitUserManager
import me.reidj.bridgebuilders.client.ClientSocket
import me.reidj.bridgebuilders.command.AdminCommand
import me.reidj.bridgebuilders.donate.DonateAdapter
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.user.Stat
import me.reidj.bridgebuilders.user.User
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmInfo
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import java.util.*
import kotlin.properties.Delegates

const val HUB = "HUB-2"

lateinit var bridgeBuildersInstance: JavaPlugin
lateinit var getByPlayer: (Player) -> User?
lateinit var getByUuid: (UUID) -> User?
lateinit var kensuke: Kensuke
lateinit var worldMeta: WorldMeta
lateinit var realm: RealmInfo
lateinit var clientSocket: ClientSocket

var slots by Delegates.notNull<Int>()
val statScope = Scope("bridge-builderssss", Stat::class.java)
var userManager = BukkitUserManager(
    listOf(statScope),
    { session: KensukeSession, context -> User(session, context.getData(statScope)) },
    { user, context -> context.store(statScope, user.stat) }
)

class BridgeBuildersInstance(
    plugin: JavaPlugin,
    byPlayer: (Player) -> User?,
    byUuid: (UUID) -> User?,
    meta: WorldMeta,
    currentSlot: Int
) {
    init {
        bridgeBuildersInstance = plugin
        worldMeta = meta


        // Подкючение к Netty сервису / Управляет конфигами, кастомными пакетами, всей data
        val bridgeServiceHost: String = getEnv("BRIDGE_SERVICE_HOST", "127.0.0.1")
        val bridgeServicePort: Int = getEnv("BRIDGE_SERVICE_PORT", "14653").toInt()
        val bridgeServicePassword: String = getEnv("BRIDGE_SERVICE_PASSWORD", "12345")

        clientSocket = ClientSocket(
            bridgeServiceHost,
            bridgeServicePort,
            bridgeServicePassword,
            Cristalix.getRealmString()
        )

        clientSocket.connect()

        // Регистрация сервисов
        val core = CoreApi.get()
        core.registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
        core.registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
        core.registerService(IInventoryService::class.java, InventoryService())

        // Конфигурация реалма
        slots = currentSlot
        realm = IRealmService.get().currentRealmInfo
        realm.status = RealmStatus.WAITING_FOR_PLAYERS
        realm.maxPlayers = currentSlot + 4
        realm.groupName = "BridgeBuilders"

        // Подключение к сервису статистики
        kensuke = BukkitKensuke.setup(bridgeBuildersInstance)
        kensuke.addGlobalUserManager(userManager)
        kensuke.globalRealm = IRealmService.get().currentRealmInfo.realmId.realmName
        userManager.isOptional = true
        kensuke.gson = GsonBuilder()
            .registerTypeHierarchyAdapter(DonatePosition::class.java, DonateAdapter())
            .create()

        getByPlayer = byPlayer
        getByUuid = byUuid

        // Регистрация админ команд
        AdminCommand()

        val nextGame = PlayerBalancer()
        B.regCommand({ player: Player, _ ->
            nextGame.accept(player)
            null
        }, "next")
    }

    private fun getEnv(name: String, defaultValue: String): String {
        var field = System.getenv(name)
        if (field == null || field.isEmpty()) {
            println("No $name environment variable specified!")
            field = defaultValue
        }
        return field
    }
}