package me.reidj.bridgebuilders

import PlayerDataManager
import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Anime
import me.func.mod.Kit
import me.func.mod.Npc
import me.func.mod.Npc.location
import me.func.mod.Npc.onClick
import me.func.mod.conversation.ModLoader
import me.func.protocol.npc.NpcBehaviour
import me.reidj.bridgebuilders.content.CustomizationNPC
import me.reidj.bridgebuilders.content.Lootbox
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.util.MapLoader
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import org.bukkit.Bukkit
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import packages.ResetRejoinPackage
import ru.cristalix.core.CoreApi
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.render.BukkitRenderService
import ru.cristalix.core.render.IRenderService
import java.util.*
import java.util.concurrent.TimeUnit

const val MOISEI: String = "ca87474e-b15c-11e9-80c4-1cb72caa35fd"
const val REIDJ: String = "bf30a1df-85de-11e8-a6de-1cb72caa35fd"

lateinit var app: App

class App : JavaPlugin() {

    private var online = 0

    private var fixDoubleClick: Player? = null

    private val hoverEvent =
        HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf<BaseComponent>(TextComponent("§eНАЖМИ НА МЕНЯ")))
    private val clickUrl = ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/crGfRk6As4")
    private val alertMessage = ComponentBuilder("\n================\n").color(ChatColor.YELLOW)
        .bold(false)
        .append("§fУ нас есть свой дискорд сервер!")
        .append("§fНе знал?")
        .event(hoverEvent)
        .event(clickUrl)
        .append("§fТогда скорее присоединяйся. §7*Клик*")
        .event(hoverEvent)
        .event(clickUrl)
        .append("\n================\n").color(ChatColor.YELLOW)
        .create()

    private val armorStand = mutableMapOf<Int, ArmorStand>()

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())

        Anime.include(Kit.EXPERIMENTAL)
        ModLoader.loadAll("mods")

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

        val core = CoreApi.get()
        core.registerService(IRenderService::class.java, BukkitRenderService(getServer()))
        core.platform.scheduler.runAsyncRepeating({
            Bukkit.getOnlinePlayers().forEach { player -> alertMessage.forEach { player.sendMessage(it) } }
        }, 10, TimeUnit.MINUTES)

        // Конфигурация реалма
        val info = IRealmService.get().currentRealmInfo
        info.status = RealmStatus.WAITING_FOR_PLAYERS
        info.maxPlayers = slots
        info.isLobbyServer = true
        info.readableName = "BridgeBuilders"
        info.groupName = "BridgeBuilders"
        info.servicedServers = arrayOf("BRI", "BRD")

        // Создание контента для лобби
        TopManager().runTaskTimer(this, 0, 1)
        CustomizationNPC

        playerDataManager = PlayerDataManager()

        B.events(
            Lootbox,
            LobbyHandler,
            GlobalListeners,
            playerDataManager
        )

        // NPC поиска игры
        worldMeta.getLabels("play").forEachIndexed { index, npcLabel ->
            val npcArgs = npcLabel.tag.split(" ")
            val stand = worldMeta.world.spawnEntity(
                npcLabel.clone().add(0.5, 2.3, 0.5),
                EntityType.ARMOR_STAND
            ) as ArmorStand
            stand.isMarker = true
            stand.isVisible = false
            stand.setGravity(false)
            stand.isCustomNameVisible = true
            armorStand[index] = stand
            Npc.npc {
                onClick {
                    val player = it.player
                    if (fixDoubleClick != null && fixDoubleClick == player)
                        return@onClick
                    fixDoubleClick = player
                    if (npcArgs[2].toInt() == 8)
                        PlayerBalancer("BRD", 8).accept(player)
                    else
                        PlayerBalancer("BRI", 16).accept(player)
                }
                name = "§e§lНАЖМИТЕ ЧТОБЫ ИГРАТЬ"
                behaviour = NpcBehaviour.STARE_AT_PLAYER
                skinUrl = "https://webdata.c7x.dev/textures/skin/$MOISEI"
                skinDigest = MOISEI
                location(npcLabel.clone().add(0.5, 0.0, 0.5))
                yaw = npcArgs[0].toFloat()
                pitch = npcArgs[1].toFloat()
            }
        }
        // Создание NPC
        val guide = worldMeta.getLabel("guide")
        val npcArgs = guide.tag.split(" ")
        Npc.npc {
            onClick { it.player.performCommand("menu") }
            location(guide.clone().add(0.5, 0.0, 0.5))
            name = "§dПерсонализация"
            behaviour = NpcBehaviour.STARE_AT_PLAYER
            skinUrl = "https://webdata.c7x.dev/textures/skin/$REIDJ"
            skinDigest = REIDJ
            yaw = npcArgs[0].toFloat()
            pitch = npcArgs[1].toFloat()
        }

        B.repeat(20) {
            info.servicedServers.forEach {
                if (RealmId.of(it).typeName == "BRI")
                    armorStand[0]?.customName = "§l4x4 §bОнлайн ${IRealmService.get().getOnlineOnRealms("BRI")}"
                else
                    armorStand[1]?.customName = "§l4х2 §bОнлайн ${IRealmService.get().getOnlineOnRealms("BRD")}"
            }
        }

        // Команда выхода в хаб
        B.regCommand({ player, _ ->
            Cristalix.transfer(listOf(player.uniqueId), RealmId.of(HUB))
            null
        }, "leave")

        // Вернуться в игру
        B.regCommand({ player: Player, _ ->
            val user = getUser(player)!!
            if (user.stat.realm == "")
                return@regCommand Formatting.error("У Вас нету незаконченной игры.")
            Cristalix.transfer(listOf(player.uniqueId), RealmId.of(user.stat.realm))
            null
        }, "rejoin")

        B.regCommand({ player: Player, _ ->
            player.setResourcePack("https://storage.c7x.ru/reidj/BridgeBuilders.zip", "100")
            null
        }, "resourcepack")

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

        Runtime.getRuntime().addShutdownHook(Thread { playerDataManager.save() })
    }

    override fun onDisable() {
        Bukkit.getOnlinePlayers().map { getUser(it)!!.stat }.forEach { it.realm = "" }
        playerDataManager.save()
    }

    fun getUser(player: Player) = getUser(player.uniqueId)

    fun getUser(uuid: UUID) = playerDataManager.userMap[uuid]
}
