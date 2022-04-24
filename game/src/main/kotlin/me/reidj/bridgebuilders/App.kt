package me.reidj.bridgebuilders

import PlayerDataManager
import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Anime
import me.func.mod.Glow
import me.func.mod.Kit
import me.func.mod.Npc
import me.func.mod.Npc.location
import me.func.mod.Npc.onClick
import me.func.mod.conversation.ModLoader
import me.func.protocol.GlowColor
import me.reidj.bridgebuilders.data.Bridge
import me.reidj.bridgebuilders.data.Team
import me.reidj.bridgebuilders.listener.*
import me.reidj.bridgebuilders.map.MapType
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.util.MapLoader
import net.minecraft.server.v1_12_R1.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import ru.cristalix.core.CoreApi
import ru.cristalix.core.datasync.EntityDataParameters
import ru.cristalix.core.formatting.Color
import ru.cristalix.core.karma.IKarmaService
import ru.cristalix.core.karma.KarmaService
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmInfo
import ru.cristalix.core.realm.RealmStatus
import java.util.*
import kotlin.math.max

const val GAMES_STREAK_RESTART = 3

lateinit var app: App

val LOBBY_SERVER: RealmId = RealmId.of("BRIL-1")
var activeStatus = Status.STARTING
var games = 0

lateinit var teams: List<Team>
lateinit var realm: RealmInfo
lateinit var map: MapType

class App : JavaPlugin() {

    override fun onEnable() {
        B.plugin = this
        app = this

        Platforms.set(PlatformDarkPaper())
        EntityDataParameters.register()

        CoreApi.get().registerService(IKarmaService::class.java, KarmaService(ISocketClient.get()))

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

        map = MapType.values().random()

        Anime.include(Kit.EXPERIMENTAL, Kit.STANDARD, Kit.NPC)
        ModLoader.loadAll("mods")

        loadMap()

        BridgeBuildersInstance(this, { getUser(it) }, worldMeta)

        // Конфигурация реалма
        realm = IRealmService.get().currentRealmInfo
        val id = IRealmService.get().currentRealmInfo.realmId.id
        realm.status = RealmStatus.WAITING_FOR_PLAYERS
        realm.extraSlots = 2
        realm.maxPlayers = slots
        realm.lobbyFallback = RealmId.of("BRIL-1")
        realm.readableName = "BridgeBuilders#$id"
        realm.groupName = "BridgeBuilders#$id"

        teams.forEach { team -> map.blocks.forEach { team.collected[it] = 0 } }

        // Запуск игрового таймера
        timer = Timer()
        timer.runTaskTimer(this, 10, 1)

        playerDataManager = PlayerDataManager()

        // Регистрация обработчиков событий
        B.events(
            GlobalListeners,
            ConnectionHandler,
            DefaultListener,
            DamageListener,
            ChatHandler,
            BlockHandler,
            playerDataManager
        )

        // Спавню нпс
        worldMeta.getLabels("builder").forEach { label ->
            val npcArgs = label.tag.split(" ")
            Npc.npc {
                onClick { event ->
                    val player = event.player
                    val user = app.getUser(player)!!
                    if (user.activeHand)
                        return@onClick
                    user.activeHand = true
                    B.postpone(5) { user.activeHand = false }
                    val team = teams.filter { it.players.contains(player.uniqueId) }[0]
                    team.collected.entries.forEachIndexed { index, block ->
                        val itemHand = player.itemInHand
                        if (itemHand.getType().getId() == block.key.material.getId() && itemHand.getData().getData() == block.key.blockData) {
                            val must = block.key.needTotal - block.value
                            if (must == 0) {
                                Anime.killboardMessage(player, "Мне больше не нужен этот ресурс")
                                player.playSound(
                                    player.location,
                                    Sound.ENTITY_ARMORSTAND_HIT,
                                    1f,
                                    1f
                                )
                                return@onClick
                            } else {
                                val subtraction = must - itemHand.getAmount()
                                val collect = must - max(0, subtraction)
                                team.collected[block.key] = block.key.needTotal - maxOf(0, subtraction)
                                itemHand.setAmount(itemHand.getAmount() - must)
                                user.collectedBlocks += collect
                                //BattlePassUtil.update(user.player!!, QuestType.POINTS, collect)
                                player.playSound(
                                    player.location,
                                    Sound.ENTITY_PLAYER_LEVELUP,
                                    1f,
                                    1f
                                )
                                teams.forEachIndexed { teamIndex, updateTeam ->
                                    Bukkit.getOnlinePlayers().forEach { online ->
                                        me.func.mod.conversation.ModTransfer(
                                            teamIndex + 2,
                                            map.needBlocks,
                                            updateTeam.collected.map { block -> block.value }.sum()
                                        ).send("bridge:progressupdate", online)
                                    }
                                }
                                // Обновление таба
                                team.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { whoSend ->
                                    Anime.killboardMessage(
                                        whoSend,
                                        "§e${player.name} §fпринёс §b${block.key.title}, §fстроительство продолжается"
                                    )
                                    me.func.mod.conversation.ModTransfer(
                                        index + 2,
                                        block.key.needTotal,
                                        block.value,
                                        map.needBlocks,
                                        team.collected.map { it.value }.sum()
                                    ).send("bridge:tabupdate", whoSend)
                                }
                            }
                        }
                    }
                }
                location(label.clone().add(0.5, 0.0, 0.5))
                behaviour = me.func.protocol.npc.NpcBehaviour.STARE_AT_PLAYER
                name = "§bСтроитель Джо"
                pitch = npcArgs[0].toFloat()
                yaw = 0f
                skinDigest = "9985b767-6677-11ec-acca-1cb72caa35fd"
                skinUrl = "https://webdata.c7x.dev/textures/skin/9985b767-6677-11ec-acca-1cb72caa35fd"
            }
        }

        // Создаю полигон
        teams.forEach { team ->
            Glow.addPlace(
                GlowColor.GREEN,
                team.teleport.x + 0.5,
                team.teleport.y,
                team.teleport.z + 0.5
            ) { player ->
                val playerTeam = teams.filter { team -> team.players.contains(player.uniqueId) }[0]
                if (!playerTeam.isActiveTeleport)
                    return@addPlace
                if (player.location.distanceSquared(playerTeam.teleport) < 4 * 4) {
                    val enemyTeam =
                        teams.filter { enemy -> !enemy.players.contains(player.uniqueId) && enemy.isActiveTeleport }
                            .random()
                    teleportAtBase(enemyTeam, player)
                    enemyTeam.players.mapNotNull { uuid -> Bukkit.getPlayer(uuid) }.forEach { enemy ->
                        enemy.playSound(
                            player.location,
                            Sound.ENTITY_ENDERDRAGON_GROWL,
                            1f,
                            1f
                        )
                    }
                } else {
                    teleportAtBase(playerTeam, player)
                }
                playerTeam.isActiveTeleport = false

                // Ставлю полоску куллдауна
                displayCoolDownBar(playerTeam)

                B.postpone(180 * 20) {
                    playerTeam.isActiveTeleport = true
                    // Отправляю сообщение о том что телепорт доступен
                    teleportAvailable(playerTeam)
                }
            }
        }

        // Создание менеджера топа
        TopManager()

        // Ломаю мосты
        teams.forEach { generateBridge(it) }

        Runtime.getRuntime().addShutdownHook(Thread { playerDataManager.save() })
    }

    fun restart() {
        Bukkit.getOnlinePlayers().filter { !isSpectator(it) }.map { app.getUser(it)!! }.forEach {
            it.stat.games++
            //me.reidj.bridgebuilders.battlepass.BattlePassUtil.update(it, me.reidj.bridgebuilders.battlepass.quest.QuestType.PLAY, 1)
            if (Math.random() < 0.11) {
                it.stat.lootbox++
                B.bc(ru.cristalix.core.formatting.Formatting.fine("§e${it.player!!.name} §fполучил §bлутбокс§f!"))
            }
        }
        playerDataManager.save()
        Bukkit.getOnlinePlayers().forEach { it.kickPlayer("Выключение сервера.") }
        Bukkit.unloadWorld(worldMeta.world, false)
        loadMap()
        teams.forEach { team ->
            team.players.clear()
            team.breakBlocks.clear()
            team.collected.clear()
            team.isActiveTeleport = false
            map.blocks.forEach { team.collected[it] = 0 }
        }
        activeStatus = Status.STARTING
        timer.time = 0
        teams.forEach { generateBridge(it) }

        // Полная перезагрузка если много игр наиграно
        if (games >= GAMES_STREAK_RESTART)
            Bukkit.shutdown()
    }

    fun addBlock(team: Team) {
        val toPlace = team.collected.filter {
            (team.bridge.blocks[it.key.material.id to it.key.blockData]
                ?: listOf()).size > it.key.needTotal - it.value
        }
        var nearest: Location? = null
        var data: Pair<Int, Byte>? = null
        team.bridge.blocks.filter { (key, _) ->
            toPlace.keys.any { it.material.id == key.first }
        }.forEach { (key, value) ->
            val tempNearest = value.minByOrNull { it.distanceSquared(team.spawn) }
            if (nearest == null || tempNearest != null) {
                nearest = tempNearest
                data = key
            }
        }
        if (nearest != null) {
            nearest?.block?.setTypeIdAndData(data!!.first, data!!.second, false)
            team.bridge.blocks[data]?.let {
                if (it.isEmpty())
                    team.bridge.blocks.remove(data)
                else
                    it.remove(nearest)
            }
        }
    }

    private fun generateBridge(team: Team): Bridge {
        val bridge = Bridge(team.bridge.toCenter, team.bridge.start, team.bridge.end, team.bridge.blocks)
        getBridge(team).forEach { current ->
            val currentBlock = current.block.type.id to current.block.data
            val blockList = bridge.blocks[currentBlock]
            if (blockList != null)
                blockList.add(current)
            else
                bridge.blocks[currentBlock] = mutableListOf(current)
            current.block.setTypeAndDataFast(0, 0)
        }
        return bridge
    }

    fun getBridge(team: Team): MutableList<Location> {
        val vector = team.bridge.toCenter
        val bridge = Bridge(vector, team.bridge.start, team.bridge.end, team.bridge.blocks)
        val width = 16
        val blockLocation = mutableListOf<Location>()

        repeat(map.length) { len ->
            repeat(width) { xOrZ ->
                repeat(map.height) { y ->
                    blockLocation.add(
                        Location(
                            worldMeta.world,
                            bridge.start.x + len * vector.x + xOrZ * vector.z,
                            bridge.start.y + y,
                            bridge.start.z + len * vector.z + xOrZ * vector.x,
                        )
                    )
                }
            }
        }
        return blockLocation
    }

    fun getCountBlocksTeam(team: Team): Boolean = team.collected.map { it.value }.sum() < map.needBlocks

    private fun loadMap() {
        worldMeta = MapLoader.load(map.title)
        teams = worldMeta.getLabels("team").map {
            val data = it.tag.split(" ")
            val team = data[0]
            Team(
                mutableListOf(),
                Color.valueOf(data.first().uppercase()),
                it,
                worldMeta.getLabel("$team-teleport"),
                data[3].toFloat(),
                data[4].toFloat(),
                false,
                com.google.common.collect.Maps.newConcurrentMap(),
                Bridge(
                    Vector(data[1].toInt(), 0, data[2].toInt()),
                    worldMeta.getLabel("$team-x"),
                    worldMeta.getLabel("$team-z"),
                ),
                mutableMapOf()
            )
        }
    }

    fun teleportAtBase(team: Team, player: Player) {
        val spawn = team.spawn
        spawn.pitch = team.pitch
        spawn.yaw = team.yaw
        player.teleport(spawn)
    }

    private fun displayCoolDownBar(team: Team) {
        team.players.mapNotNull { Bukkit.getPlayer(it) }
            .forEach {
                Anime.reload(it, 0.1, "До следующего телепорта", 42, 102, 240)
                B.postpone(20 * 2) { Anime.reload(it, 180.0, "До следующего телепорта", 42, 102, 240) }
            }
    }

    private fun teleportAvailable(team: Team) {
        team.players.mapNotNull { Bukkit.getPlayer(it) }
            .forEach {
                Anime.killboardMessage(it, "Телепорт на чужие базы теперь §aдоступен")
                it.playSound(
                    it.location,
                    Sound.BLOCK_PORTAL_AMBIENT,
                    1.5f,
                    1.5f
                )
            }
    }

    override fun onDisable() {
        playerDataManager.save()

        val worlds = Bukkit.getWorlds()

        println("Start finding any world to find directory.")

        worlds.forEach { world ->
            println("Start cleaning up worlds.")

            val dir = world.worldFolder.parentFile.walk(FileWalkDirection.TOP_DOWN)
                .firstOrNull { it.name.contains("tmp") }
            dir?.listFiles { file -> file.isDirectory && file.parentFile == dir }
                ?.filter { it.canRead() }
                ?.firstOrNull { file -> worlds.none { it.uid.toString() == file.name } }?.let { file ->
                    println("Start cleaning up worlds. File: ${file.absolutePath}")

                    worlds.firstOrNull { it.uid.toString() == file.name }?.let {
                        Bukkit.unloadWorld(it, false)
                        println("World was unloaded.")
                    }

                    MinecraftServer.SERVER.postToNextTick {
                        if (file.deleteRecursively()) println("World directory successfully deleted.")
                        else println("World directory delete failure.")
                    }
                }
        }
    }

    fun isSpectator(player: Player): Boolean = player.gameMode == GameMode.SPECTATOR

    fun getUser(player: Player): User? = getUser(player.uniqueId)

    fun getUser(uuid: UUID): User? = playerDataManager.userMap[uuid]

    private fun getEnv(name: String, defaultValue: String): String {
        var field = System.getenv(name)
        if (field == null || field.isEmpty()) {
            println("No $name environment variable specified!")
            field = defaultValue
        }
        return field
    }

    fun updateNumbersPlayersInTeam() = teams.forEach { team ->
        team.players.mapNotNull { Bukkit.getPlayer(it) }.forEach {
            Anime.bottomRightMessage(it, "Игроков в команде §8>> §a${team.players.size}")
        }
    }
}