package me.reidj.bridgebuilders

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Anime
import me.func.mod.Kit
import me.reidj.bridgebuilders.content.Lootbox
import me.reidj.bridgebuilders.data.BlockPlan
import me.reidj.bridgebuilders.data.Bridge
import me.reidj.bridgebuilders.data.Team
import me.reidj.bridgebuilders.listener.ConnectionHandler
import me.reidj.bridgebuilders.listener.DamageListener
import me.reidj.bridgebuilders.listener.DefaultListener
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.map.MapType
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.util.ArrowEffect
import me.reidj.bridgebuilders.util.MapLoader
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material.AIR
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import ru.cristalix.core.datasync.EntityDataParameters
import ru.cristalix.core.formatting.Color
import ru.cristalix.core.realm.RealmId
import java.util.*

const val GAMES_STREAK_RESTART = 6

lateinit var app: App

val map = MapLoader.load(MapType.AQUAMARINE.data.title)
val LOBBY_SERVER: RealmId = RealmId.of("BRIL-1")
var activeStatus = Status.STARTING
var games = 0

val teams = map.getLabels("team").map {
    val data = it.tag.split(" ")
    val team = data[0]
    println(map.getLabel("$team-x"))
    println(map.getLabel("$team-z"))
    Team(
        mutableListOf(),
        Color.valueOf(data.first().uppercase()),
        it,
        map.getLabel("$team-teleport"),
        false,
        mutableMapOf(),
        Bridge(
            Vector(data[1].toInt(), 0, data[2].toInt()),
            map.getLabel("$team-x"),
            map.getLabel("$team-z"),
        ),
        mutableMapOf()
    )
}

class App : JavaPlugin() {

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())
        EntityDataParameters.register()

        Anime.include(Kit.EXPERIMENTAL, Kit.STANDARD, Kit.NPC)

        BridgeBuildersInstance(this, { getUser(it) }, { getUser(it) }, map, 4)
        realm.readableName = "BridgeBuilders ${realm.realmId.id}"
        realm.lobbyFallback = LOBBY_SERVER

        teams.forEach { team -> BlockPlan.values().forEach { team.collected[it] = 0 } }

        // Регистрация обработчиков событий
        B.events(
            GlobalListeners,
            ConnectionHandler,
            DefaultListener,
            DamageListener,
            Lootbox
        )

        // Рисую эффект выстрела
        ArrowEffect().arrowEffect(this)

        // Запуск игрового таймера
        timer = Timer()
        timer.runTaskTimer(this, 10, 1)

        TopManager()

        teams.forEach { generateBridge(it) }
    }

    fun restart() {
        activeStatus = Status.STARTING
        Bukkit.getOnlinePlayers().forEach { it.kickPlayer("Выключение сервера.") }
        Bukkit.unloadWorld(map.name, false)

        // Полная перезагрузка если много игр наиграно
        if (games > GAMES_STREAK_RESTART)
            Bukkit.shutdown()
    }

    fun getUser(player: Player): User = getUser(player.uniqueId)

    fun getUser(uuid: UUID): User = userManager.getUser(uuid)

    fun addBlock(team: Team) {
        val toPlace = team.collected.filter {
            (team.bridge.blocks[it.key.material.id to it.key.blockData] ?: listOf()).size > it.key.needTotal - it.value
        }
        var nearest: Location? = null
        var data: Pair<Int, Byte>? = null
        team.bridge.blocks.filter { (key, location) ->
            toPlace.keys.any { it.material.id == key.first && it.blockData == key.second }
                    && location.any { it.block.type == AIR }
        }
            .forEach { (key, value) ->
                val tempNearest = value.minByOrNull { it.distanceSquared(team.spawn) }
                if (nearest == null || (tempNearest != null && nearest != null && tempNearest.block.type == AIR &&
                            tempNearest.distanceSquared(team.spawn) < nearest!!.distanceSquared(team.spawn))
                ) {
                    nearest = tempNearest
                    data = key
                }
            }
        if (nearest != null && data != null) {
            nearest?.block?.setTypeIdAndData(data!!.first, data!!.second, false)
            team.bridge.blocks[data]?.let {
                if (it.size <= 1)
                    team.bridge.blocks.remove(data)
                else
                    it.remove(nearest)
            }
        }
    }

    private fun generateBridge(team: Team): Bridge {
        val vector = Vector(1, 0, 0)
        val bridge = Bridge(vector, team.bridge.start, team.bridge.end, team.bridge.blocks)

        getBridge(team).forEach { current ->
            if (current.block.type == AIR)
                return@forEach
            val currentBlock = current.block.type.id to current.block.data
            val blockList = bridge.blocks[currentBlock]
            if (blockList != null) {
                blockList.add(current)
            } else {
                bridge.blocks[currentBlock] = mutableListOf(current)
            }
            current.block.setTypeAndDataFast(0, 0)
        }
        return bridge
    }

    fun getBridge(team: Team): MutableList<Location> {
        val vector = Vector(1, 0, 0)
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
                            map.world,
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
}