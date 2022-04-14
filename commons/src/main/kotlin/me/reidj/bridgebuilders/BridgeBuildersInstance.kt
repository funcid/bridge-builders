package me.reidj.bridgebuilders

import PlayerDataManager
import clepto.bukkit.B
import clepto.cristalix.Cristalix
import clepto.cristalix.WorldMeta
import me.reidj.bridgebuilders.command.AdminCommand
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
import kotlin.properties.Delegates

const val HUB = "HUB-2"

lateinit var bridgeBuildersInstance: JavaPlugin
lateinit var getByPlayer: (Player) -> User?
lateinit var worldMeta: WorldMeta
lateinit var realm: RealmInfo
lateinit var clientSocket: client.ClientSocket
lateinit var playerDataManager: PlayerDataManager

var slots by Delegates.notNull<Int>()

class BridgeBuildersInstance(
    plugin: JavaPlugin,
    byPlayer: (Player) -> User?,
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

        clientSocket = client.ClientSocket(
            bridgeServiceHost,
            bridgeServicePort,
            bridgeServicePassword,
            Cristalix.getRealmString()
        )

        clientSocket.connect()

        // Регистрация сервисов
        CoreApi.get().apply {
            registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
            registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
            registerService(IInventoryService::class.java, InventoryService())
        }

        // Конфигурация реалма
        slots = currentSlot
        realm = IRealmService.get().currentRealmInfo
        realm.status = RealmStatus.WAITING_FOR_PLAYERS
        realm.maxPlayers = currentSlot + 4
        realm.groupName = "BridgeBuilders"

        getByPlayer = byPlayer

        // Регистрация админ команд
        AdminCommand()

        playerDataManager = PlayerDataManager()

        B.repeat(1) { clientSocket }
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