package me.reidj.bridgebuilders

import clepto.bukkit.B
import me.func.mod.Anime
import me.func.mod.conversation.ModTransfer
import me.reidj.bridgebuilders.data.Team
import me.reidj.bridgebuilders.donate.impl.StarterKit
import me.reidj.bridgebuilders.util.DefaultKit
import me.reidj.bridgebuilders.util.WinUtil
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Color.*
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.realm.RealmStatus.GAME_STARTED_CAN_SPACTATE

val kit = DefaultKit

enum class Status(val lastSecond: Int, val now: (Int) -> Int) {
    STARTING(70, { it ->
        // Если набор игроков начался, обновить статус реалма
        realm.status = RealmStatus.GAME_STARTED_RESTRICTED

        val players = Bukkit.getOnlinePlayers()

        // Обновление шкалы онлайна
        players.forEach { player -> ModTransfer(slots, players.size, true).send("bridge:online", player) }
        var actualTime = it

        // Если время вышло и пора играть
        if (it / 20 == STARTING.lastSecond) {
            // Начать отсчет заново, так как мало игроков
            if (players.size < slots) {
                actualTime = 1
            } else {
                // Обновление статуса реалма, чтобы нельзя было войти
                realm.status = GAME_STARTED_CAN_SPACTATE
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
                B.postpone(10) {
                    // Телепортация игроков
                    teams.forEachIndexed { index, team ->
                        val color = checkColor(team.color)
                        Bukkit.getOnlinePlayers().forEach {
                            // Отправка прогресса команд
                            ModTransfer(
                                index + 2,
                                color.getRed(),
                                color.getGreen(),
                                color.getBlue()
                            ).send("bridge:progressinit", it)
                        }

                        app.updateNumbersPlayersInTeam()

                        team.players.forEach {
                            val player = Bukkit.getPlayer(it) ?: return@forEach
                            val user = app.getUser(it)!!

                            player.gameMode = org.bukkit.GameMode.SURVIVAL
                            player.itemOnCursor = null

                            app.teleportAtBase(team, player)

                            player.customName = "${team.color.chatColor}[${
                                team.color.teamName.substring(
                                    0,
                                    1
                                )
                            }] ${me.reidj.bridgebuilders.listener.ChatHandler.getPrefix(user, true)}"

                            player.inventory.armorContents = kit.armor.map { armor ->
                                val meta = armor.itemMeta as org.bukkit.inventory.meta.LeatherArmorMeta
                                meta.color = checkColor(team.color)
                                armor.itemMeta = meta
                                armor
                            }.toTypedArray()

                            player.inventory.addItem(kit.sword, kit.pickaxe, kit.axe, kit.spade, kit.bread)

                            StarterKit.valueOf(user.stat.activeKit.name).content.forEach { starter ->
                                player.inventory.addItem(starter)
                            }

                            // Отправка таба
                            team.collected.entries.forEachIndexed { index, block ->
                                ModTransfer(
                                    index + 2,
                                    block.key.needTotal,
                                    block.value,
                                    block.key.title,
                                    block.key.getItem()
                                ).send("bridge:init", player)
                            }

                            Anime.alert(
                                player,
                                "Цель",
                                "Принесите нужные блоки строителю, \nчтобы построить мост к центру"
                            )
                            me.func.mod.Glow.showAllPlaces(player)
                        }
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
        // Обновление шкалы времени
        if (time % 20 == 0) {
            Bukkit.getOnlinePlayers().forEach {
                ModTransfer(GAME.lastSecond, time, false).send("bridge:online", it)
                if (time / 20 == 120) {
                    teams.forEach { team -> team.isActiveTeleport = true }
                    Anime.killboardMessage(it, "Телепорт на чужие базы теперь §aдоступен")
                }
                if (time / 20 == 600) {
                    Anime.alert(it, "Сброс мира", "Некоторые блоки начали регенерироваться...")
                    teams.forEach { team -> team.blockReturn() }
                }
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