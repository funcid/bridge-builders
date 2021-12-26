package me.reidj.bridgebuilders

import me.func.mod.Anime
import me.func.mod.Npc.onClick
import me.func.protocol.Marker
import me.reidj.bridgebuilders.data.DefaultKit
import org.bukkit.Bukkit
import org.bukkit.GameMode
import ru.cristalix.core.realm.RealmStatus.GAME_STARTED_CAN_JOIN
import ru.cristalix.core.realm.RealmStatus.GAME_STARTED_RESTRICTED

lateinit var winMessage: String
const val needPlayers: Int = 3
val kit = DefaultKit
val markers: MutableList<Marker> = mutableListOf()

enum class Status(val lastSecond: Int, val now: (Int) -> Int) {
    STARTING(5, { it ->
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
                    team.players.forEach { it ->
                        val player = Bukkit.getPlayer(it) ?: return@forEach
                        val user = getByPlayer(player)
                        player.gameMode = GameMode.SURVIVAL
                        player.itemOnCursor = null
                        player.teleport(team.spawn)
                        player.inventory.armorContents = kit.armor
                        player.inventory.addItem(kit.sword, kit.pickaxe, kit.bread)
                        team.team!!.addEntry(player.name)

                        team.requiredBlocks.entries.forEachIndexed { index, block ->
                            me.reidj.bridgebuilders.mod.ModTransfer()
                                .integer(index + 1)
                                .integer(block.value.needTotal)
                                .integer(block.value.collected)
                                .string(block.value.title)
                                .item(block.value.getItem(block.value.item, block.value.id))
                                .send("bridge:init", user)
                        }
                        map.getLabels("builder").forEach { label ->
                            val npcArgs = label.tag.split(" ")
                            val npc = me.func.mod.Npc.npc {
                                onClick {
                                    teams.filter { it.players.contains(player.uniqueId) }
                                        .forEach { team ->
                                            team.requiredBlocks.entries.forEachIndexed { index, block ->
                                                val itemHand = player.itemInHand
                                                if (itemHand.i18NDisplayName == block.value.getItem(
                                                        block.value.item,
                                                        block.value.id
                                                    ).i18NDisplayName
                                                ) {
                                                    val must = block.value.needTotal - block.value.collected
                                                    println(must)
                                                    if (must == 0) {
                                                        me.reidj.bridgebuilders.mod.ModHelper.notification(
                                                            user,
                                                            ru.cristalix.core.formatting.Formatting.error("Мне больше не нужен этот ресурс")
                                                        )
                                                        return@forEach
                                                    } else {
                                                        block.value.collected =
                                                            block.value.needTotal - maxOf(
                                                                0,
                                                                must - itemHand.getAmount()
                                                            )
                                                        itemHand.setAmount(itemHand.getAmount() - must)
                                                    }
                                                    team.players.forEach { _ ->
                                                        me.reidj.bridgebuilders.listener.DefaultListener.sum += 1
                                                        me.reidj.bridgebuilders.mod.ModTransfer()
                                                            .integer(index + 1)
                                                            .integer(block.value.needTotal)
                                                            .integer(block.value.collected)
                                                            .integer(170)
                                                            .integer(me.reidj.bridgebuilders.listener.DefaultListener.sum)
                                                            .send(
                                                                "bridge:tabupdate",
                                                                user
                                                            )
                                                    }
                                                    player.updateInventory()
                                                }
                                            }
                                        }
                                }
                                x = label.x + 0.5
                                y = label.y
                                z = label.z + 0.5
                                behaviour = me.func.protocol.npc.NpcBehaviour.STARE_AT_PLAYER
                                name = "§bСтроитель Джо"
                                yaw = npcArgs[0].toFloat()
                                skinDigest = "4a9df40e-e0ca-11e8-8374-1cb72caa35fd"
                                skinUrl = "https://webdata.c7x.dev/textures/skin/4a9df40e-e0ca-11e8-8374-1cb72caa35fd"
                            }.spawn()
                            npc.show(player)
                        }

                        Anime.alert(
                            player,
                            "Цель",
                            "Принесите нужные блоки строителю, \nчтобы построить мост к центру"
                        )

                        markers.add(
                            Anime.marker(
                                player,
                                Marker(
                                    java.util.UUID.randomUUID(),
                                    team.teleport.x + 0.5,
                                    team.teleport.y + 1.5,
                                    team.teleport.z + 0.5,
                                    16.0,
                                    me.func.protocol.MarkerSign.ARROW_DOWN.texture
                                )
                            )
                        )

                        markers.forEach { marker ->
                            me.func.mod.Glow.addPlace(
                                me.func.protocol.GlowColor.GREEN,
                                marker.x,
                                marker.y - 1.5,
                                marker.z
                            )
                            var up = false
                            clepto.bukkit.B.repeat(15) {
                                up = !up
                                Anime.moveMarker(
                                    player,
                                    marker.uuid,
                                    marker.x,
                                    marker.y - if (up) 0.0 else 0.7,
                                    marker.z,
                                    0.75
                                )
                            }
                        }
                        me.func.mod.Glow.showAllPlaces(player)
                    }
                }
                // Список игроков
                val users = players.map { app.getUser(it) }
                users.forEach { user ->
                    // Отправить информацию о начале игры клиенту
                    me.reidj.bridgebuilders.mod.ModTransfer().send("bridge:start", user)
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
    GAME(330, { time ->
        // Обновление шкалы времени
        if (time % 20 == 0) {
            Bukkit.getOnlinePlayers().forEach {
                me.reidj.bridgebuilders.mod.ModTransfer()
                    .integer(GAME.lastSecond)
                    .integer(time)
                    .boolean(false)
                    .send("bridge:online", app.getUser(it))
            }
            if (time == 180)
                me.reidj.bridgebuilders.mod.ModHelper.allNotification("Телепорт на чужие базы теперь §aдоступен")
        }
        // Проверка на победу
        if (me.reidj.bridgebuilders.util.WinUtil.check4win()) {
            activeStatus = END
        }
        time
    }),
    END(340, { time ->
        if (GAME.lastSecond * 20 + 10 == time) {
            Bukkit.getOnlinePlayers().forEach {
                val user = app.getUser(it)
                user.stat.games++
            }
        }
        teams.forEach {
            it.players.forEach { player ->
                try {
                    val find = Bukkit.getPlayer(player)
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