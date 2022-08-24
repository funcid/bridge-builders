package me.reidj.bridgebuilders

import clepto.cristalix.WorldMeta
import me.func.mod.Anime
import me.func.mod.util.command
import me.reidj.bridgebuilders.donate.impl.NameTagType
import me.reidj.bridgebuilders.protocol.BulkSaveUserPackage
import me.reidj.bridgebuilders.protocol.SaveUserPackage
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.permissions.IGroup
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.realm.RealmId
import java.util.*
import kotlin.math.sqrt

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

private val barrier = ItemStack(Material.BARRIER)

lateinit var worldMeta: WorldMeta

val clientSocket: ISocketClient = ISocketClient.get()

val userMap = mutableMapOf<UUID, User>()

lateinit var plugin: JavaPlugin

val godSet = hashSetOf(
    "307264a1-2c69-11e8-b5ea-1cb72caa35fd", // Func
    "bf30a1df-85de-11e8-a6de-1cb72caa35fd", // Reidj
    "ca87474e-b15c-11e9-80c4-1cb72caa35fd", // Moisei
    "0e7c0015-b27b-11eb-acca-1cb72caa35fd" // 3а6ив
)

private const val LEVEL_FORMAT = "%.0f из %.0f"

fun getLobbyRealm(): RealmId = RealmId.of("BRIL-1")

fun getUser(player: Player): User? = getUser(player.uniqueId)

fun getUser(uuid: UUID): User? = userMap[uuid]

fun createDisplayName(user: User): String {
    val prefix: String
    val permissionService = IPermissionService.get()
    val id = user.stat.uuid
    val staffGroup = permissionService.getStaffGroup(id).get()
    val donateGroup = permissionService.getDonateGroup(id).get()
    prefix = if (staffGroup.prefix.isEmpty()) prefix(donateGroup) else prefix(staffGroup)
    val color = permissionService.getPermissionContext(id).get().color
        ?: if ("PLAYER" != staffGroup.name) staffGroup.nameColor else donateGroup.nameColor

    return (if (user.stat.currentNameTag != NameTagType.NONE.name) NameTagType.valueOf(user.stat.currentNameTag)
        .getTitle() + " §8┃ " else "") + (if (prefix.isEmpty()) "" else "$prefix §8┃ ") + color + user.cachedPlayer?.name
}

private fun prefix(group: IGroup): String = if (group.prefix.isEmpty()) "" else group.prefixColor + group.prefix

fun regAdminCommand(commandName: String, executor: (User, Array<out String>) -> Unit) {
    command(commandName) { player, args ->
        if (player.isOp || player.uniqueId.toString() in godSet) {
            val user = getUser(player) ?: return@command
            executor(user, args)
            player.sendMessage(Formatting.fine("Успешно!"))
        } else {
            player.sendMessage(Formatting.error("Нет прав."))
        }
    }
}

fun bulkSave(remove: Boolean): BulkSaveUserPackage? =
    BulkSaveUserPackage(Bukkit.getOnlinePlayers().map {
        val uuid = it.uniqueId
        val user = (if (remove) userMap.remove(uuid) else userMap[uuid]) ?: return null
        user.inGame = false
        SaveUserPackage(uuid, user.stat)
    }.toList())

fun getRequiredExperience(forLevel: Int) = forLevel * forLevel - forLevel / 2

fun getLevel(experience: Double) = ((sqrt(5.0) * sqrt(experience * 80 + 5) + 5) / 20).toInt() + 1

fun Player.error(title: String, subTitle: String) = Anime.itemTitle(this@error, barrier, title, subTitle, 2.0)

fun Player.isSpectator() = player.gameMode == GameMode.SPECTATOR

fun getTexture(name: String) = "minecraft:mcpatcher/cit/simulators/${name.lowercase()}.png"