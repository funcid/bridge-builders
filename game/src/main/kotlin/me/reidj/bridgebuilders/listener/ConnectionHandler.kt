package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import me.func.mod.Npc
import me.func.mod.Npc.onClick
import me.reidj.bridgebuilders.*
import me.reidj.bridgebuilders.mod.ModHelper
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.cristalix.core.item.Items
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmStatus

object ConnectionHandler : Listener {

    private val back = item {
        type = Material.CLAY_BALL
        nbt("other", "cancel")
        text("§cВернуться")
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE

        val user = getByPlayer(player)

        B.postpone(5) {
            // Спавню нпс
            map.getLabels("builder").forEach { label ->
                val npcArgs = label.tag.split(" ")
                Npc.npc {
                    onClick { event ->
                        val player = event.player
                        if (user.activeHand)
                            return@onClick
                        user.activeHand = true
                        B.postpone(5) { user.activeHand = false }
                        teams.filter { it.players.contains(player.uniqueId) }
                            .forEach { team ->
                                team.collected.entries.forEachIndexed { index, block ->
                                    val itemHand = player.itemInHand
                                    if (itemHand.i18NDisplayName == block.key.getItem().i18NDisplayName) {
                                        val must = block.key.needTotal - block.value
                                        if (must == 0) {
                                            ModHelper.notification(
                                                user,
                                                ru.cristalix.core.formatting.Formatting.error("Мне больше не нужен этот ресурс")
                                            )
                                            player.playSound(
                                                player.location,
                                                org.bukkit.Sound.ENTITY_ARMORSTAND_HIT,
                                                1f,
                                                1f
                                            )
                                            return@onClick
                                        } else {
                                            val subtraction = must - itemHand.getAmount()
                                            team.collected[block.key] =
                                                block.key.needTotal - maxOf(0, subtraction)
                                            val brought = must - subtraction
                                            itemHand.setAmount(itemHand.getAmount() - must)

                                            user.collectedBlocks += brought
                                            player.playSound(
                                                player.location,
                                                org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
                                                1f,
                                                1f
                                            )
                                        }
                                        team.players.forEach { uuid ->
                                            ModHelper.notification(
                                                getByUuid(
                                                    uuid
                                                ),
                                                "§e${player.name} §fпринёс §b${block.key.title}, §fстроительство продолжается"
                                            )
                                            me.reidj.bridgebuilders.mod.ModTransfer()
                                                .integer(index + 2)
                                                .integer(block.key.needTotal)
                                                .integer(block.value)
                                                .integer(4096)
                                                .integer(team.players.map { getByUuid(it) }
                                                    .sumOf { it.collectedBlocks })
                                                .send(
                                                    "bridge:tabupdate",
                                                    getByUuid(uuid)
                                                )
                                        }
                                        player.updateInventory()
                                        return@onClick
                                    }
                                }
                            }
                    }
                    x = label.x + 0.5
                    y = label.y
                    z = label.z + 0.5
                    behaviour = me.func.protocol.npc.NpcBehaviour.STARE_AT_PLAYER
                    name = "§bСтроитель Джо"
                    pitch = npcArgs[0].toFloat()
                    yaw = 0f
                    skinDigest = "9985b767-6677-11ec-acca-1cb72caa35fd"
                    skinUrl = "https://webdata.c7x.dev/textures/skin/9985b767-6677-11ec-acca-1cb72caa35fd"
                }.spawn(player)
            }
        }

        user.stat.lastEnter = System.currentTimeMillis()

        if (activeStatus == Status.STARTING) {
            player.inventory.setItem(8, back)
            teams.forEach {
                player.inventory.addItem(
                    Items.builder()
                        .displayName("Выбрать команду: " + it.color.chatFormat + it.color.teamName)
                        .type(Material.WOOL)
                        .color(it.color)
                        .build()
                )
            }
        }

    }

    @EventHandler
    fun PlayerQuitEvent.handle() {
        val user = getByPlayer(player)
        teams.filter { it.players.contains(player.uniqueId) }.forEach { it.players.remove(player.uniqueId) }
        user.stat.timePlayedTotal += System.currentTimeMillis() - user.stat.lastEnter
    }

    @EventHandler
    fun AsyncPlayerPreLoginEvent.handle() {
        if (activeStatus != Status.STARTING) {
            playerProfile.properties.forEach { profileProperty ->
                if (profileProperty.value == "PARTY_WARP") {
                    if (IRealmService.get().currentRealmInfo.status != RealmStatus.WAITING_FOR_PLAYERS) {
                        disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Сейчас нельзя зайти на этот сервер")
                        loginResult = AsyncPlayerPreLoginEvent.Result.KICK_OTHER
                    }
                }
            }
        }
    }
}