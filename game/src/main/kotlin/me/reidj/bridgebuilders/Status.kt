package me.reidj.bridgebuilders

import me.reidj.bridgebuilders.data.DefaultKit
import org.bukkit.Bukkit
import org.bukkit.FireworkEffect
import org.bukkit.GameMode
import org.bukkit.entity.Firework
import ru.cristalix.core.realm.RealmStatus.GAME_STARTED_CAN_JOIN
import ru.cristalix.core.realm.RealmStatus.GAME_STARTED_RESTRICTED

lateinit var winMessage: String
const val needPlayers: Int = 3
val kit = DefaultKit
enum class Status(val lastSecond: Int, val now: (Int) -> Int) {
    STARTING(30, { it ->
        // Если набор игроков начался, обновить статус реалма
        if (it == 40)
            realm.status = GAME_STARTED_CAN_JOIN

        val players = Bukkit.getOnlinePlayers()
        // Обновление шкалы онлайна
        players.forEach {
            me.reidj.bridgebuilders.mod.ModTransfer()
                .integer(slots)
                .integer(players.size)
                .boolean(true)
                .send("bridge:online", app.getUser(it))
        }
        var actualTime = it

        // Если время вышло и пора играть
        if (it / 20 == STARTING.lastSecond) {
            // Начать отсчет заново, так как мало игроков
            if (players.size + needPlayers < slots)
                actualTime = 1
            else {
                // Обновление статуса реалма, чтобы нельзя было войти
                realm.status = GAME_STARTED_RESTRICTED
                games++
                // Удаление игроков если они оффлайн
                teams.forEach {
                    it.players.removeIf { player ->
                        val craftPlayer = Bukkit.getPlayer(player)
                        craftPlayer == null || !craftPlayer.isOnline
                    }
                }
                // Заполение команд
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.inventory.clear()
                    player.openInventory.topInventory.clear()
                    if (!teams.any { it.players.contains(player.uniqueId) })
                        teams.minByOrNull { it.players.size }!!.players.add(player.uniqueId)
                }
                // Телепортация игроков
                teams.forEach { team ->
                    team.players.forEach {
                        val player = Bukkit.getPlayer(it) ?: return@forEach
                        player.gameMode = GameMode.SURVIVAL
                        player.itemOnCursor = null
                        player.teleport(team.location)
                        player.inventory.armorContents = kit.armor
                        player.inventory.addItem(kit.sword, kit.pickaxe, kit.bread)
                        team.team!!.addEntry(player.name)
                    }
                }
                // Список игроков
                val users = players.map { app.getUser(it) }
                users.forEach { user ->
                    // Отправить информацию о начале игры клиенту
                    me.reidj.bridgebuilders.mod.ModTransfer().send("bridge:start", user)
                }
                // Выдача активных ролей
                activeStatus = GAME
                actualTime + 1
            }
        }
        // Если набралось максимальное количество игроков, то сократить время ожидания до 10 секунд
        if (players.size == slots && it / 20 < STARTING.lastSecond - 10)
            actualTime = (STARTING.lastSecond - 10) * 20
        actualTime
    }),
    GAME(330, { time ->
        // Обновление шкалы времени
        if (time % 20 == 0) {
            Bukkit.getOnlinePlayers().forEach {
                me.reidj.bridgebuilders.mod.ModTransfer()
                    .integer(GAME.lastSecond)
                    .integer(time)
                    .boolean(false)
                    .send("bridge:online", app.getUser(it))
                me.reidj.bridgebuilders.teams.forEach { team ->
                    me.reidj.bridgebuilders.mod.ModTransfer()
                        .double(team.teleportLocation.x)
                        .double(team.teleportLocation.y)
                        .double(team.teleportLocation.z)
                        .double(team.rotate)
                        .send("bridge:teleportupdate", app.getUser(it))
                }
            }
        }
        // Проверка на победу
        if (me.reidj.bridgebuilders.util.WinUtil.check4win()) {
            activeStatus = END
        }
        time
    }),
    END(340, { time ->
        if (GAME.lastSecond * 20 + 10 == time) {
            // Выдача побед выжившим и выдача всем доп. игр
            Bukkit.getOnlinePlayers().forEach {
                val user = app.getUser(it)
                if (it.gameMode != GameMode.SPECTATOR) {
                    user.stat.wins++
                    val firework = it.world!!.spawn(it.location, Firework::class.java)
                    val meta = firework.fireworkMeta
                    meta.addEffect(
                        FireworkEffect.builder()
                            .flicker(true)
                            .trail(true)
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .with(FireworkEffect.Type.BALL)
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withColor(org.bukkit.Color.YELLOW)
                            .withColor(org.bukkit.Color.GREEN)
                            .withColor(org.bukkit.Color.WHITE)
                            .build()
                    )
                    meta.power = 0
                    firework.fireworkMeta = meta
                }
                user.stat.games++
            }
        }
        teams.forEach {
            it.players.forEach { player ->
                try {
                    val find = org.bukkit.Bukkit.getPlayer(player)
                    if (find != null)
                        it.team!!.removePlayer(find)
                } catch (ignored: Exception) {
                }
            }
            it.players.clear()
        }
        when {
            time == GAME.lastSecond * 20 + 20 * 10 -> {
                app.restart()
                -1
            }
            time < (END.lastSecond - 10) * 20 -> (END.lastSecond - 10) * 20
            else -> time
        }
    }),
}