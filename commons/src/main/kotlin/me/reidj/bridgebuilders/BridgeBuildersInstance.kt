package me.reidj.bridgebuilders

import PlayerDataManager
import clepto.bukkit.B
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
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService

const val HUB = "HUB-2"

lateinit var bridgeBuildersInstance: JavaPlugin
lateinit var getByPlayer: (Player) -> User?
lateinit var worldMeta: WorldMeta
lateinit var clientSocket: client.ClientSocket
lateinit var playerDataManager: PlayerDataManager
var slots: Int = 16

class BridgeBuildersInstance(
    plugin: JavaPlugin,
    byPlayer: (Player) -> User?,
    meta: WorldMeta
) {
    init {
        bridgeBuildersInstance = plugin
        worldMeta = meta

        // Регистрация сервисов
        CoreApi.get().apply {
            registerService(IPartyService::class.java, PartyService(ISocketClient.get()))
            registerService(ITransferService::class.java, TransferService(ISocketClient.get()))
            registerService(IInventoryService::class.java, InventoryService())
        }

        getByPlayer = byPlayer

        // Регистрация админ команд
        AdminCommand()

        B.repeat(1) { clientSocket }
    }
}