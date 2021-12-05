package me.reidj.bridgebuilders

import clepto.bukkit.B
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.func.mod.Anime
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
            1 to RequiredBlock("Камень",1, 2),
            2 to RequiredBlock("Земля", 0, 5)
        )
    )
}

class App : JavaPlugin() {

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())
        teams = teams.dropLast(teams.size - 4)

        Anime.initStandardMods()

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

        B.events(GlobalListeners)

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