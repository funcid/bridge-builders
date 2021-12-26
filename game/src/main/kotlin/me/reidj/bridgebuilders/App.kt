package me.reidj.bridgebuilders

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Anime
import me.func.mod.Kit
import me.reidj.bridgebuilders.data.RequiredBlock
import me.reidj.bridgebuilders.data.Team
import me.reidj.bridgebuilders.listener.ConnectionHandler
import me.reidj.bridgebuilders.listener.DamageListener
import me.reidj.bridgebuilders.listener.DefaultListener
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.map.MapType
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.util.MapLoader
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.formatting.Color
import ru.cristalix.core.realm.RealmId
import ru.cristalix.npcs.server.Npcs
import java.util.*

const val GAMES_STREAK_RESTART = 6

lateinit var app: App

val map = MapLoader.load(MapType.AQUAMARINE.data.title)
val LOBBY_SERVER: RealmId = RealmId.of("BRIL-1")
var activeStatus = Status.STARTING
var games = 0

var teams = listOf(
    Color.RED,
    Color.BLUE,
    Color.GREEN,
    Color.YELLOW
).map {
    Team(
        mutableListOf(),
        it,
        map.getLabel(it.name.toLowerCase() + "-team"),
        map.getLabel(it.name.toLowerCase() + "-teleport"),
        null,
        true,
        mutableMapOf(),
        mutableMapOf(
            1 to RequiredBlock("Булыжная ограда", 0, 2, Material.COBBLE_WALL, 0),
            2 to RequiredBlock("Еловый забор", 0, 4, Material.SPRUCE_FENCE, 0),
            3 to RequiredBlock("Песчаник", 0, 5, Material.SANDSTONE, 0),
            4 to RequiredBlock("Призмарин", 0,20, Material.PRISMARINE, 0),
            5 to RequiredBlock("Еловые доски", 0, 30, Material.WOOD, 1),
            6 to RequiredBlock("Фиолетовая керамика", 0, 48, Material.STAINED_CLAY, 10),
            7 to RequiredBlock("Песчаниковые ступеньки", 0, 82, Material.SANDSTONE_STAIRS, 0),
            8 to RequiredBlock("Бирюзовый бетон", 0, 90, Material.CONCRETE, 9),
            9 to RequiredBlock("Булыжные ступеньки", 0, 118, Material.COBBLESTONE_STAIRS, 0),
            10 to RequiredBlock("Дубовые ступеньки", 0, 164, Material.WOOD_STAIRS,0),
            11 to RequiredBlock("Еловые ступеньки", 0, 232, Material.SPRUCE_WOOD_STAIRS, 0),
            12 to RequiredBlock("Люк", 0, 302, Material.TRAP_DOOR, 0),
            13 to RequiredBlock("Песок", 0, 312, Material.SAND, 0),
            14 to RequiredBlock("Песчаниковая плита", 0, 317, Material.STEP, 1),
            15 to RequiredBlock("Бирюзовый цемент", 0, 420, Material.CONCRETE_POWDER, 9),
            16 to RequiredBlock("Еловая плита", 0, 440, Material.WOOD_STEP, 1),
            17 to RequiredBlock("Андезит", 0, 792, Material.STONE, 5),
            18 to RequiredBlock("Дубовая плита", 0, 10, Material.WOOD_STEP, 0),
        ),
        0
    )
}

class App : JavaPlugin() {

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())
        teams = teams.dropLast(teams.size - 4)

        Anime.include(Kit.EXPERIMENTAL, Kit.STANDARD, Kit.NPC)

        BridgeBuildersInstance(this, { getUser(it) }, { getUser(it) }, map, 4)
        realm.readableName = "BridgeBuilders ${realm.realmId.id}"
        realm.lobbyFallback = LOBBY_SERVER

        // Регистрация обработчиков событий
        B.events(
            GlobalListeners,
            ConnectionHandler,
            DefaultListener,
            DamageListener
        )

        // Запуск игрового таймера
        timer = Timer()
        timer.runTaskTimer(this, 10, 1)

        TopManager()
        Npcs.init(this)

        // Скорборд команды
        val manager = Bukkit.getScoreboardManager()
        val board = manager.newScoreboard
        teams.forEach {
            it.team = board.registerNewTeam(it.color.teamName)
            it.team!!.color = org.bukkit.ChatColor.valueOf(it.color.name)
            it.team!!.setAllowFriendlyFire(false)
            it.team!!.prefix = "" + it.color.chatColor
            it.team!!.setOption(
                org.bukkit.scoreboard.Team.Option.COLLISION_RULE,
                org.bukkit.scoreboard.Team.OptionStatus.NEVER
            )
        }
    }

    fun restart() {
        activeStatus = Status.STARTING
        Bukkit.getOnlinePlayers().forEach { it.kickPlayer("Выключение сервера.") }
        Bukkit.unloadWorld(map.name, false)

        // Полная перезагрузка если много игр наиграно
        if (games > GAMES_STREAK_RESTART)
            Bukkit.shutdown()
    }

    fun getUser(player: Player) = getUser(player.uniqueId)

    fun getUser(uuid: UUID) = userManager.getUser(uuid)
}