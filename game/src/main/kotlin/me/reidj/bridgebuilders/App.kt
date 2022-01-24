package me.reidj.bridgebuilders

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import implario.ListUtils
import me.func.mod.Anime
import me.func.mod.Banners
import me.func.mod.Kit
import me.reidj.bridgebuilders.content.Lootbox
import me.reidj.bridgebuilders.data.BlockPlan
import me.reidj.bridgebuilders.data.Bridge
import me.reidj.bridgebuilders.data.Team
import me.reidj.bridgebuilders.listener.*
import me.reidj.bridgebuilders.map.MapType
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.util.ArrowEffect
import me.reidj.bridgebuilders.util.MapLoader
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import ru.cristalix.core.datasync.EntityDataParameters
import ru.cristalix.core.formatting.Color
import ru.cristalix.core.realm.RealmId
import java.util.*
import java.util.stream.Collectors

const val GAMES_STREAK_RESTART = 6

lateinit var app: App

val LOBBY_SERVER: RealmId = RealmId.of("TEST-56")
var activeStatus = Status.STARTING
var games = 0

lateinit var teams: List<Team>

class App : JavaPlugin() {

    override fun onEnable() {
        B.plugin = this
        app = this
        loadMap()
        Platforms.set(PlatformDarkPaper())
        EntityDataParameters.register()

        Anime.include(Kit.EXPERIMENTAL, Kit.STANDARD, Kit.NPC)

        BridgeBuildersInstance(this, { getUser(it) }, { getUser(it) }, worldMeta, 16)
        realm.readableName = "BridgeBuilders ${realm.realmId.id}"
        realm.lobbyFallback = LOBBY_SERVER

        teams.forEach { team -> BlockPlan.values().forEach { team.collected[it] = 0 } }

        // Запуск игрового таймера
        timer = Timer()
        timer.runTaskTimer(this, 10, 1)

        // Регистрация обработчиков событий
        B.events(
            GlobalListeners,
            ConnectionHandler,
            DefaultListener,
            DamageListener,
            ChatHandler,
            BlockHandler,
            Lootbox
        )

        // Создаю полигон
        teams.forEach { team ->
            me.func.mod.Glow.addPlace(
                me.func.protocol.GlowColor.GREEN,
                team.teleport.x + 0.5,
                team.teleport.y,
                team.teleport.z + 0.5
            ) { player ->
                if (!team.isActiveTeleport)
                    return@addPlace
                var enemyTeam: Team? = null
                val playerTeam = teams.filter { team -> team.players.contains(player.uniqueId) }
                if (player.location.distanceSquared(playerTeam[0].teleport) < 4 * 4) {
                    enemyTeam = ListUtils.random(teams.stream()
                        .filter { enemy -> !enemy.players.contains(player.uniqueId) }
                        .collect(Collectors.toList()))
                    teleportAtBase(enemyTeam, player)
                    enemyTeam.players.map { uuid -> Bukkit.getPlayer(uuid) }.forEach { enemy ->
                        enemy.playSound(
                            player.location,
                            Sound.ENTITY_ENDERDRAGON_GROWL,
                            1f,
                            1f
                        )
                    }
                } else {
                    teleportAtBase(playerTeam[0], player)
                }
                enemyTeam?.isActiveTeleport = false
                playerTeam[0].isActiveTeleport = false

                // Ставлю полоску куллдауна
                enemyTeam?.let { displayCoolDownBar(it) }
                displayCoolDownBar(playerTeam[0])

                B.postpone(180 * 20) {
                    enemyTeam?.isActiveTeleport = true
                    playerTeam[0].isActiveTeleport = true

                    // Отправляю сообщение о том что телепорт доступен
                    enemyTeam?.let { teleportAvailable(it) }
                    teleportAvailable(playerTeam[0])
                }
            }
        }

        // Создание баннера
        Banners.new {
            x = 6.0
            y = 97.0
            z = -1.2
            opacity = 0.0
            content = "Сломай меня"
            height = 10
            weight = 10
            watchingOnPlayer = true
        }

        // Рисую эффект выстрела
        ArrowEffect().arrowEffect(this)

        // Создание менеджера топа
        TopManager()

        // Ломаю мосты
        teams.forEach { generateBridge(it) }
    }

    fun restart() {
        Bukkit.getOnlinePlayers().forEach {
            val user = app.getUser(it)
            user.stat.games++
            if (Math.random() < 0.11) {
                user.stat.lootbox++
                B.bc(ru.cristalix.core.formatting.Formatting.fine("§e${user.player!!.name} §fполучил §bлутбокс§f!"))
            }
        }
        Bukkit.getOnlinePlayers().forEach { it.kickPlayer("Выключение сервера.") }
        Bukkit.unloadWorld(worldMeta.world, false)
        loadMap()
        teams.forEach { team ->
            team.players.clear()
            team.breakBlocks.clear()
            team.collected.clear()
            team.isActiveTeleport = false
            BlockPlan.values().forEach { team.collected[it] = 0 }
        }
        activeStatus = Status.STARTING
        timer.time = 0
        teams.forEach { generateBridge(it) }

        // Полная перезагрузка если много игр наиграно
        if (games > GAMES_STREAK_RESTART)
            Bukkit.shutdown()
    }

    fun getUser(player: Player): User {
        return getUser(player.uniqueId)
    }

    fun getUser(uuid: UUID): User {
        return userManager.getUser(uuid)
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
            if (nearest == null || (tempNearest != null &&
                        tempNearest.distanceSquared(team.spawn) < nearest!!.distanceSquared(team.spawn))
            ) {
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
        val length = 84
        val width = 16
        val height = 30
        val blockLocation = mutableListOf<Location>()

        repeat(length) { len ->
            repeat(width) { xOrZ ->
                repeat(height) { y ->
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

    fun getCountBlocksTeam(team: Team): Boolean {
        if (team.players.map { getByUuid(it) }
                .sumOf { it.collectedBlocks } < 3639)
            return true
        return false
    }

    private fun loadMap() {
        worldMeta = MapLoader().load(MapType.AQUAMARINE.data.title)
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
        team.players.map { Bukkit.getPlayer(it) }.forEach { Anime.reload(it, 180.0, "Перезарядка...", 42, 102, 240) }
    }

    private fun teleportAvailable(team: Team) {
        team.players.map { Bukkit.getPlayer(it) }
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
}