package me.reidj.bridgebuilders

import clepto.bukkit.B
import clepto.cristalix.WorldMeta
import me.reidj.bridgebuilders.command.AdminCommand
import me.reidj.bridgebuilders.donate.impl.NameTag
import me.reidj.bridgebuilders.packages.BulkSaveUserPackage
import me.reidj.bridgebuilders.packages.SaveUserPackage
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.inventory.IInventoryService
import ru.cristalix.core.inventory.InventoryService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartyService
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.transfer.ITransferService
import ru.cristalix.core.transfer.TransferService
import java.util.*

const val HUB = "HUB-2"

lateinit var bridgeBuildersInstance: JavaPlugin
lateinit var getByPlayer: (Player) -> User?
lateinit var worldMeta: WorldMeta
var clientSocket: ISocketClient = ISocketClient.get()
var slots: Int = System.getenv("SLOT").toInt()

val userMap = mutableMapOf<UUID, User>()

private val permissionService: IPermissionService = IPermissionService.get()

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
            registerService(IPartyService::class.java, PartyService(clientSocket))
            registerService(ITransferService::class.java, TransferService(clientSocket))
            registerService(IInventoryService::class.java, InventoryService())
        }

        getByPlayer = byPlayer

        // Регистрация админ команд
        AdminCommand()

        B.repeat(1) { clientSocket }
    }
}


fun getEnv(name: String, defaultValue: String): String {
    var field = System.getenv(name)
    if (field == null || field.isEmpty()) {
        println("No $name environment variable specified!")
        field = defaultValue
    }
    return field
}

fun isSpectator(player: Player): Boolean = player.gameMode == GameMode.SPECTATOR

fun getPrefix(user: User, isTab: Boolean): String {
    var finalPrefix = ""
    permissionService.getBestGroup(user.stat.uuid).thenAccept { group ->
        permissionService.getNameColor(user.stat.uuid).thenApply {
            finalPrefix =
                (if (user.stat.activeNameTag == me.reidj.bridgebuilders.data.NameTag.NONE) "" else NameTag.valueOf(user.stat.activeNameTag.name)
                    .getRare()
                    .getColor() + NameTag.valueOf(user.stat.activeNameTag.name)
                    .getTitle() + "§8 ┃ ") + (if (group.prefix == "") "" else group.nameColor + group.prefix + "§8 ┃ §f") + (it
                    ?: group.nameColor) + user.player!!.name + if (!isTab) " §8${Formatting.ARROW_SYMBOL + group.chatMessageColor} " else ""
        }
    }
    return finalPrefix
}

fun save(): BulkSaveUserPackage =
    BulkSaveUserPackage(Bukkit.getOnlinePlayers().map {
        val uuid = it.uniqueId
        val user = userMap.remove(uuid)
        SaveUserPackage(uuid, user?.stat)
    })
