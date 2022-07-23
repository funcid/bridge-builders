package me.reidj.bridgebuilders

import PlayerDataManager
import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Anime
import me.func.mod.Kit
import me.reidj.bridgebuilders.command.PlayerCommands
import me.reidj.bridgebuilders.content.CustomizationNPC
import me.reidj.bridgebuilders.content.Lootbox
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.listener.LobbyHandler
import me.reidj.bridgebuilders.npc.NpcManager
import me.reidj.bridgebuilders.ticker.detail.BanUtil
import me.reidj.bridgebuilders.ticker.detail.DiscordMessage
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.util.MapLoader
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import packages.ResetRejoinPackage
import ru.cristalix.core.CoreApi
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.render.BukkitRenderService
import ru.cristalix.core.render.IRenderService
import java.util.*

const val STORAGE = "https://storage.c7x.dev/reidj/"

lateinit var app: App

class App : JavaPlugin() {

    lateinit var lootbox: Lootbox

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())

        Anime.include(Kit.EXPERIMENTAL, Kit.STANDARD, Kit.NPC, Kit.LOOTBOX)

        BridgeBuildersInstance(this, { getUser(it) }, MapLoader.load("LOBB"))

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

        clientSocket.registerHandler(ResetRejoinPackage::class.java) { pckg ->
            val user = getUser(pckg.uuid) ?: return@registerHandler
            user.stat.realm = ""
        }

        CoreApi.get().registerService(IRenderService::class.java, BukkitRenderService(getServer()))

        // Конфигурация реалма
        IRealmService.get().currentRealmInfo.run {
            status = RealmStatus.WAITING_FOR_PLAYERS
            maxPlayers = slots
            isLobbyServer = true
            readableName = "BridgeBuilders"
            groupName = "BridgeBuilders"
            servicedServers = arrayOf("BRI", "BRD")
        }

        // Создание контента для лобби
        TopManager().runTaskTimer(this, 0, 1)
        CustomizationNPC
        lootbox = Lootbox()

        // Инициализация команд
        PlayerCommands

        // Обработка каждого тика
        TickTimerHandler(DiscordMessage, NpcManager, BanUtil, lootbox).runTaskTimer(this, 0, 1)

        playerDataManager = PlayerDataManager()

        B.events(
            lootbox,
            LobbyHandler,
            GlobalListeners,
            playerDataManager
        )
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().map { getUser(it)!!.stat }.forEach { it.realm = "" }
        playerDataManager.save()
    }

    fun getUser(player: Player) = getUser(player.uniqueId)

    fun getUser(uuid: UUID) = playerDataManager.userMap[uuid]
}
