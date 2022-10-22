package me.reidj.node.game

import kotlinx.coroutines.runBlocking
import me.func.mod.Anime
import me.func.mod.conversation.ModTransfer
import me.func.mod.ui.Glow
import me.func.mod.util.after
import me.func.mod.util.listener
import me.func.mod.world.Npc
import me.func.mod.world.Npc.location
import me.func.mod.world.Npc.onClick
import me.func.protocol.data.color.GlowColor
import me.func.protocol.data.status.EndStatus
import me.func.protocol.world.npc.NpcBehaviour
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.data.LootBoxType
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.util.MapLoader
import me.reidj.node.activeStatus
import me.reidj.node.map.MapType
import me.reidj.node.team.Bridge
import me.reidj.node.team.Team
import me.reidj.node.teams
import me.reidj.node.timer.Status
import me.reidj.node.timer.Timer
import me.reidj.node.util.everyAfter
import org.bukkit.Bukkit
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import ru.cristalix.core.formatting.Color
import ru.cristalix.core.formatting.Formatting
import ru.cristalix.core.transfer.ITransferService
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

const val GAMES_STREAK_RESTART = 5

class BridgeGame {

    val timer = Timer(this)

    lateinit var mapType: MapType

    var games = 0

    init {
        startGame()
        spawnBuilder()

        listener(
            ConnectionHandler(this),
            InteractHandler,
            UnusedListeners(),
            BlockHandler(this),
            ChatHandler(),
            PlayerMoveHandler(this),
            DamageHandler()
        )

        everyAfter(10, 1) { timer.tick() }
    }

    private fun startGame() {
        loadMap()
        teams = worldMeta.getLabels("team").map {
            val data = it.tag.split(" ")
            val team = data[0]
            Team(
                mutableSetOf(),
                Color.valueOf(data.first().uppercase()),
                it,
                worldMeta.getLabel("$team-teleport"),
                data[3].toFloat(),
                data[4].toFloat(),
                Bridge(
                    Vector(data[1].toInt(), 0, data[2].toInt()),
                    worldMeta.getLabel("$team-x"),
                    worldMeta.getLabel("$team-z"),
                ),
                mutableMapOf(),
                mutableMapOf()
            )
        }.toMutableList()
        teams.forEach { team ->
            team.bridge.generateBridge(mapType)
            mapType.blocks.forEach { team.collected[it] = 0 }
        }
    }

    private fun loadMap() {
        mapType = MapType.values().random()
        worldMeta = MapLoader().load(mapType.address).apply {
            after(40) {
                world.setGameRuleValue("doDaylightCycle", "false")
                world.fullTime = 18000
            }
        }
    }

    fun stopGame() {
        ITransferService.get().transferBatch(Bukkit.getOnlinePlayers().map { it.uniqueId }, getLobbyRealm())

        after(5) {
            /*realm.status = RealmStatus.WAITING_FOR_PLAYERS
            activeStatus = Status.STARTING
            timer.time = 0
            teams.clear()
            userMap.values.forEach { it.inGame = false }
            userMap.clear()
            games++
            Bukkit.unloadWorld(worldMeta.world, false)
            startGame()*/
            Bukkit.shutdown()
        }
    }

    fun getSpawnLocation(): Location = worldMeta.getLabel("spawn").clone().apply {
        x += 0.5
        z += 0.5
    }

    fun teleportAvailable(player: Player) {
        Anime.killboardMessage(player, "§fТелепорт на чужие базы теперь §aдоступен")
        player.playSound(
            player.location,
            Sound.BLOCK_PORTAL_AMBIENT,
            1.5f,
            1.5f
        )
    }

    private fun spawnBuilder() {
        worldMeta.getLabels("builder").forEach { label ->
            val npcArgs = label.tag.split(" ")
            Npc.npc {
                location(label.apply {
                    x += 0.5
                    z += 0.5
                })
                behaviour = NpcBehaviour.STARE_AT_PLAYER
                name = "§bСтроитель Джо"
                pitch = npcArgs[0].toFloat()
                yaw = 0f
                skinDigest = "9985b767-6677-11ec-acca-1cb72caa35fd"
                skinUrl = "https://webdata.c7x.dev/textures/skin/9985b767-6677-11ec-acca-1cb72caa35fd"
                onClick { event ->
                    val player = event.player
                    val user = getUser(player) ?: return@onClick
                    if (user.isArmLock)
                        return@onClick
                    user.isArmLock = true
                    after(5) { user.isArmLock = false }
                    val team = teams.firstOrNull { player.uniqueId in it.players } ?: return@onClick
                    val itemInHand = player.itemInHand
                    team.collected.entries.forEachIndexed { index, entry ->
                        if (entry.key.getItem(itemInHand.getAmount()) == itemInHand) {
                            val must = entry.key.needTotal - entry.value
                            if (must == 0) {
                                Anime.bigTitle(player, "§7Этот ресурс больше не нужен!")
                                Glow.animate(player, 0.6, GlowColor.RED)
                                player.playSound(player.location, Sound.ENTITY_ARMORSTAND_HIT, 1f, 1f)
                            } else {
                                val subtraction = must - itemInHand.getAmount()
                                val collect = must - max(0, subtraction)
                                val message =
                                    "§e${player.name} §fпринёс §b${entry.key.title}, §fстроительство продолжается!"

                                team.collected[entry.key] = entry.key.needTotal - maxOf(0, subtraction)
                                team.blocksToPlace += collect

                                Glow.animate(player, 0.6, GlowColor.GREEN)

                                itemInHand.setAmount(itemInHand.getAmount() - must)

                                user.collectedBlocks += collect

                                player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)

                                team.players.mapNotNull { Bukkit.getPlayer(it) }.forEach { allies ->
                                    allies.sendMessage("${team.commandPrefix} $message")
                                    Anime.killboardMessage(allies, message)
                                    // Обновление таба
                                    ModTransfer(
                                        index + 2,
                                        entry.key.needTotal,
                                        entry.value,
                                        mapType.needBlocks,
                                        team.collected.map { it.value }.sum()
                                    ).send("bridge:tabupdate", allies)
                                }
                                // Обновление прогресса команд
                                teams.forEachIndexed { teamIndex, updateTeam ->
                                    Bukkit.getOnlinePlayers().forEach { online ->
                                        ModTransfer(
                                            teamIndex + 2,
                                            mapType.needBlocks,
                                            updateTeam.collected.map { block -> block.value }.sum()
                                        ).send("bridge:progressupdate", online)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun blockNextToNpc(location: Location) = Npc.npcs.any {
        sqrt(
            (it.value.data.x - location.x).pow(2.0) + (it.value.data.y - location.y).pow(
                2.0
            ) + (it.value.data.z - location.z).pow(2.0)
        ) <= 5
    }

    fun checkWin(): Boolean {
        // Если время вышло
        if ((activeStatus.lastSecond * 18 == timer.time))
            return true
        return false
    }

    fun end(team: Team) {
        team.players.mapNotNull { getUser(it) }.forEach {
            it.run {
                givePureEther(30)
                givePureExperience(30)
                cachedPlayer?.let { player ->
                    player.sendMessage(Formatting.fine("Вы получили §d30 Эфира §fи §b30 опыта §fза победу."))
                    Anime.showEnding(
                        player,
                        EndStatus.WIN,
                        listOf("Блоков принесено:", "Игроков убито:"),
                        listOf("$collectedBlocks", "$cacheKills")
                    )
                    player.world!!.spawn(player.location, Firework::class.java).run {
                        fireworkMeta = fireworkMeta.apply {
                            addEffect(
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
                            power = 0
                        }
                    }
                }
                stat.wins++
            }
        }
        teams.filter { it != team }.forEach { looser ->
            looser.players.mapNotNull { getUser(it) }.forEach {
                it.run {
                    givePureEther(15)
                    giveExperience(15)
                    cachedPlayer?.let { player ->
                        player.sendMessage(Formatting.fine("Вы получили §d15 Эфира §fи §b15 опыта§f."))
                        Anime.showEnding(
                            player,
                            EndStatus.LOSE,
                            listOf("Блоков принесено:", "Игроков убито:"),
                            listOf("${it.collectedBlocks}", "$cacheKills")
                        )
                    }
                }
            }
        }
        Bukkit.getOnlinePlayers().filter { !it.isSpectator() }.mapNotNull { getUser(it) }.onEach {
            it.run {
                stat.games++
                inGame = false
                if (Math.random() < 0.02) {
                    val lootBox = LootBoxType.values().drop(5).random()
                    LootBoxType.values().drop(5).forEach { println(it) }
                    stat.lootBoxes.add(lootBox)
                    Bukkit.broadcastMessage(Formatting.fine("§e${if (cachedPlayer == null) "ERROR" else cachedPlayer!!.name} §fполучил ${lootBox.lootBox.rare.getColored()} лутбокс§f!"))
                }
            }
        }
        after(5) {
            runBlocking { clientSocket.write(bulkSave(true)) }
            Thread.sleep(1000)
        }
        after(40) { stopGame() }
    }

    fun standardOperations(player: Player, user: User, playerTeam: Team) {
        after { ModTransfer(false).send("bridge:online", player) }

        playerTeam.baseTeleport(player)

        teams.forEachIndexed { index, team ->
            val color = org.bukkit.Color.fromRGB(team.color.color)
            Bukkit.getOnlinePlayers().forEach {
                // Отправка прогресса команд
                ModTransfer(
                    index + 2,
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue()
                ).send("bridge:progressinit", it)
            }
            Bukkit.getOnlinePlayers().forEach { online ->
                ModTransfer(
                    index + 2,
                    mapType.needBlocks,
                    team.collected.map { block -> block.value }.sum()
                ).send("bridge:progressupdate", online)
            }
        }
        playerTeam.collected.entries.forEachIndexed { index, block ->
            // Заполнение таба
            ModTransfer(
                index + 2,
                block.key.needTotal,
                block.value,
                block.key.title,
                block.key.getItem(1)
            ).send("bridge:init", player)
            // Отправка прогресса
            ModTransfer(
                index + 2,
                block.key.needTotal,
                block.value,
                mapType.needBlocks,
                playerTeam.collected.map { it.value }.sum()
            ).send("bridge:tabupdate", player)
        }

        player.gameMode = org.bukkit.GameMode.SURVIVAL

        Anime.timer(player, "Игра закончится через", Status.GAME.lastSecond)

        player.customName = "${playerTeam.color.chatColor}[${
            playerTeam.color.teamName.substring(
                0,
                1
            )
        }] ${createDisplayName(user)}"
    }
}