package me.reidj.bridgebuilders

import clepto.bukkit.B
import data.StarterKit
import me.func.mod.Anime
import me.func.mod.conversation.ModTransfer
import me.reidj.bridgebuilders.listener.BlockHandler
import me.reidj.bridgebuilders.team.Team
import me.reidj.bridgebuilders.util.DefaultKit
import me.reidj.bridgebuilders.util.WinUtil
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Color.*
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.realm.RealmStatus.GAME_STARTED_CAN_SPACTATE

private val kit = DefaultKit
private val fastDigging = PotionEffect(PotionEffectType.FAST_DIGGING, Int.MAX_VALUE, 1)

enum class Status(val lastSecond: Int, val now: (Int) -> Int) {
    STARTING(70, { it ->
        // Если набор игроков начался, обновить статус реалма
        realm.status = RealmStatus.GAME_STARTED_RESTRICTED

        val players = Bukkit.getOnlinePlayers()

        // Обновление шкалы онлайна
        players.forEach { player -> ModTransfer(slots, players.size).send("bridge:online", player) }
        var actualTime = it

        // Если время вышло и пора играть
        if (it / 20 == STARTING.lastSecond) {
            // Начать отсчет заново, так как мало игроков
            if (players.size < slots) {
                actualTime = 1
            } else {
                // Обновление статуса реалма, чтобы нельзя было войти
                realm.status = GAME_STARTED_CAN_SPACTATE
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
                    app.updateNumbersPlayersInTeam()

                    team.players.mapNotNull { Bukkit.getPlayer(it) }.forEach uuid@{
                        val user = app.getUser(it)!!

                        user.inGame = true
                        user.team = team

                        DefaultKit.init(it)

                        Anime.timer(it, "Конец игры через", GAME.lastSecond)
                        Anime.sendEmptyBuffer("online:hide", it)

                        it.inventory.armorContents = kit.armor.map { armor ->
                            val meta = armor.itemMeta as LeatherArmorMeta
                            meta.color = checkColor(team.color)
                            armor.itemMeta = meta
                            armor
                        }.toTypedArray()

                        if (user.stat.activeKit == StarterKit.NONE)
                            it.inventory.addItem(kit.sword, kit.pickaxe, kit.axe, kit.spade, kit.bread)
                        else
                            me.reidj.bridgebuilders.donate.impl.StarterKit.valueOf(user.stat.activeKit.name).content.forEach { starter ->
                                it.inventory.addItem(starter)
                            }

                        Anime.alert(
                            it,
                            "Цель",
                            "Принесите нужные блоки строителю, \nчтобы построить мост к центру\nи разрушить маяк"
                        )
                        me.func.mod.Glow.showAllPlaces(it)
                    }
                }
                // Список игроков
                val users = players.mapNotNull { app.getUser(it) }
                users.forEach { user ->
                    // Отправить информацию о начале игры клиенту
                    ModTransfer().send("bridge:start", user.player)
                }
                activeStatus = GAME
                actualTime + 1
            }
        }
        // Если набралось максимальное количество игроков, то сократить время ожидания до 10 секунд
        if (players.size == slots && it / 20 < STARTING.lastSecond - 10)
            actualTime = (STARTING.lastSecond - 10) * 20
        actualTime
    }),
    GAME(2500, { time ->
        if (time % 20 == 0) {
            if (time / 20 == 120 && activeStatus == Status.GAME) {
                teams.forEach { team -> team.isActiveTeleport = true }
                Bukkit.getOnlinePlayers()
                    .forEach { Anime.killboardMessage(it, "Телепорт на чужие базы теперь §aдоступен") }
            }
            if (time / 20 == 600 && activeStatus == Status.GAME) {
                Bukkit.getOnlinePlayers()
                    .forEach { Anime.alert(it, "Сброс мира", "Некоторые блоки начали регенерироваться...") }
                teams.forEach { team -> team.blockReturn() }
                BlockHandler.placedBlocks.clear()
            }
        }
        // Проверка на победу
        if (WinUtil.check4win()) {
            ru.cristalix.core.karma.IKarmaService.get().enableGG { true }
            val teamsWithBlockCount: MutableMap<Team, Int> = mutableMapOf()
            // Получаю команды с количеством принесённых блоков
            teams.forEach { teamsWithBlockCount[it] = it.collected.map { collected -> collected.value }.sum() }

            B.postpone(5 * 20) {
                teamsWithBlockCount.clear()
                app.restart()
            }

            // Победившая команда
            WinUtil.end(teamsWithBlockCount.entries.sortedBy { -it.value }.subList(0, 1)[0].key)
        }
        time
    }),
    END(340, { time ->
        when {
            time == GAME.lastSecond * 20 + 20 * 10 -> {
                app.restart()
                -1
            }
            time < (END.lastSecond - 10) * 20 -> (END.lastSecond - 10) * 20
            else -> time
        }
    }),
    ;
}

fun checkColor(color: ru.cristalix.core.formatting.Color): Color {
    return when (color) {
        ru.cristalix.core.formatting.Color.YELLOW -> YELLOW
        ru.cristalix.core.formatting.Color.RED -> RED
        ru.cristalix.core.formatting.Color.GREEN -> GREEN
        ru.cristalix.core.formatting.Color.BLUE -> BLUE
        else -> throw NullPointerException("Color is null.")
    }
}