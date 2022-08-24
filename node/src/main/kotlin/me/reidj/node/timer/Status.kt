package me.reidj.node.timer

import me.func.mod.Anime
import me.reidj.bridgebuilders.donate.impl.StartingKit
import me.reidj.bridgebuilders.getUser
import me.reidj.node.activeStatus
import me.reidj.node.block_regeneration.RegenerationManager
import me.reidj.node.game.BridgeGame
import me.reidj.node.realm
import me.reidj.node.slots
import me.reidj.node.teams
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.inventory.meta.LeatherArmorMeta
import ru.cristalix.core.realm.RealmStatus

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class Status(val lastSecond: Int, val now: (Int, BridgeGame) -> Int) {
    STARTING(70, { time, game ->
        val players = Bukkit.getOnlinePlayers()

        var actualTime = time

        // Если время вышло и пора играть
        if (time / 20 >= STARTING.lastSecond && players.size >= slots) {
            // Начать отсчет заново, так как мало игроков
            if (players.size < slots) {
                actualTime = 1
            } else {
                // Обновление статуса реалма, чтобы нельзя было войти
                realm.status = RealmStatus.GAME_STARTED_CAN_SPACTATE
                // Удаление игроков если они оффлайн
                teams.forEach {
                    it.players.removeIf { uuid ->
                        val player = Bukkit.getPlayer(uuid)
                        player == null || !player.isOnline
                    }
                }
                // Заполение команд
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.inventory.clear()
                    player.openInventory.topInventory.clear()
                    player.inventory.itemInOffHand.setAmount(0)
                    player.itemOnCursor.setAmount(0)
                    if (!teams.any { it.players.contains(player.uniqueId) })
                        teams.minByOrNull { it.players.size }!!.players.add(player.uniqueId)
                }
                teams.forEach team@{ team ->
                    team.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
                        team.updateNumbersPlayersInTeam()

                        me.reidj.node.game.ModifiersManager.modifierAccept(player)

                        Anime.alert(
                            player,
                            "Цель",
                            "Принесите нужные блоки строителю, \nчтобы построить мост к центру\nи разрушить маяк"
                        )

                        val user = getUser(player) ?: return@team
                        val kit = StartingKit.valueOf(user.stat.currentStarterKit)

                        user.inGame = true

                        game.standardOperations(player, user, team)

                        player.inventory.armorContents = if (kit == StartingKit.NONE) kit.armorContent.map {
                            it.itemMeta =
                                (it.itemMeta as LeatherArmorMeta).apply { color = Color.fromRGB(team.color.color) }
                            it
                        }.toTypedArray() else kit.armorContent
                        player.inventory.addItem(*kit.inventory)
                    }
                }
                activeStatus = GAME
                actualTime + 1
            }
        }
        // Если набралось максимальное количество игроков, то сократить время ожидания до 10 секунд
        if (players.size == slots && time / 20 < STARTING.lastSecond - 10) actualTime =
            (STARTING.lastSecond - 10) * 20
        actualTime
    }),
    GAME(2500, { time, game ->
        if (time % 20 == 0) {
            val players = Bukkit.getOnlinePlayers()
            if (time / 20 == 120 && activeStatus == GAME) {
                players.mapNotNull { getUser(it) }.forEach {
                    it.isTeleportAvailable = true
                    it.cachedPlayer?.let { player ->
                        Anime.killboardMessage(
                            player,
                            "Телепорт на чужие базы теперь §aдоступен"
                        )
                    }
                }
            } else if (time / 20 == 600) {
                RegenerationManager.placeBlock()
                players.forEach { Anime.alert(it, "Сброс мира", "Некоторые блоки начали регенерироваться...") }
            }
        }
        if (game.checkWin())
            activeStatus = END
        else if (Bukkit.getOnlinePlayers().isEmpty())
            game.stopGame()
        time
    }),
    END(2510, { time, game ->
        if (GAME.lastSecond * 20 + 10 == time) {
            game.end(teams.map { it to it.collected.map { collected -> collected.value }.sum() }
                .sortedBy { -it.second }.subList(0, 1).first().first)
        }
        when {
            time == GAME.lastSecond * 20 + 20 * 10 -> {
                -1
            }

            time < (END.lastSecond - 10) * 20 -> (END.lastSecond - 10) * 20
            else -> time
        }
    })
    ;
}