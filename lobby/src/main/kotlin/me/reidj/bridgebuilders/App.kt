package me.reidj.bridgebuilders

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Anime
import me.func.mod.Kit
import me.func.mod.data.Sprites
import me.func.mod.selection.button
import me.func.mod.selection.choicer
import me.reidj.bridgebuilders.command.PlayerCommands
import me.reidj.bridgebuilders.content.CustomizationNPC
import me.reidj.bridgebuilders.content.Lootbox
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.listener.LobbyHandler
import me.reidj.bridgebuilders.npc.NpcManager
import me.reidj.bridgebuilders.npc.NpcType
import me.reidj.bridgebuilders.ticker.detail.BanUtil
import me.reidj.bridgebuilders.ticker.detail.CompassUpdateOnline
import me.reidj.bridgebuilders.ticker.detail.DiscordMessage
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.util.MapLoader
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.render.BukkitRenderService
import ru.cristalix.core.render.IRenderService
import java.util.*

const val STORAGE = "https://storage.c7x.dev/reidj/"

lateinit var app: App

val compass = choicer {
    title = "BridgeBuilders"
    description = "Собери предметы для постройки моста!"
    buttons(
        button {
            texture = Sprites.DUO.path()
            title = "§b4x2"
            description = "Онлайн: §3" + IRealmService.get().getOnlineOnRealms("BRD")
            onClick { it, _, _ -> it.performCommand(NpcType.TWO.command) }
        },
        button {
            texture = Sprites.SQUAD.path()
            title = "§b4x4"
            description = "Онлайн: §3" + IRealmService.get().getOnlineOnRealms("BRI")
            onClick { it, _, _ -> it.performCommand(NpcType.FOUR.command) }
        }
    )
}

class App : JavaPlugin() {

    lateinit var lootbox: Lootbox

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())

        Anime.include(Kit.EXPERIMENTAL, Kit.STANDARD, Kit.NPC, Kit.LOOTBOX, Kit.GRAFFITI)

        BridgeBuildersInstance(this, { getUser(it) }, MapLoader.load("LOBB2"))

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
        TickTimerHandler(DiscordMessage, NpcManager, BanUtil, lootbox, CompassUpdateOnline).runTaskTimer(this, 0, 1)

        B.events(
            lootbox,
            LobbyHandler,
            GlobalListeners,
        )
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().map { getUser(it)!!.stat }.forEach { it.realm = "" }
        save()
    }

    fun getUser(player: Player) = getUser(player.uniqueId)

    fun getUser(uuid: UUID) = userMap[uuid]
}
