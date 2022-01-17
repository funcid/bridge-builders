package me.reidj.bridgebuilders

import clepto.bukkit.B
import clepto.cristalix.Cristalix
import dev.implario.bukkit.platform.Platforms
import dev.implario.platform.impl.darkpaper.PlatformDarkPaper
import me.reidj.bridgebuilders.content.CustomizationNPC
import me.reidj.bridgebuilders.content.Lootbox
import me.reidj.bridgebuilders.listener.GlobalListeners
import me.reidj.bridgebuilders.top.TopManager
import me.reidj.bridgebuilders.util.MapLoader
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.cristalix.core.CoreApi
import ru.cristalix.core.display.data.DataDrawData
import ru.cristalix.core.display.data.StringDrawData
import ru.cristalix.core.math.V2
import ru.cristalix.core.math.V3
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.render.BukkitRenderService
import ru.cristalix.core.render.IRenderService
import ru.cristalix.core.render.VisibilityTarget
import ru.cristalix.core.render.WorldRenderData
import ru.cristalix.npcs.data.NpcBehaviour
import ru.cristalix.npcs.server.Npc
import ru.cristalix.npcs.server.Npcs
import java.util.*

lateinit var app: App
const val SKIN: String = "bf30a1df-85de-11e8-a6de-1cb72caa35fd"

class App : JavaPlugin() {

    override fun onEnable() {
        B.plugin = this
        app = this
        Platforms.set(PlatformDarkPaper())

        BridgeBuildersInstance(this, { getUser(it) }, { getUser(it) }, MapLoader.load("prod"), 200)

        CoreApi.get().registerService(IRenderService::class.java, BukkitRenderService(getServer()))

        // Конфигурация реалма
        realm.isLobbyServer = false
        realm.readableName = "BridgeBuilders Lobby"
        realm.servicedServers = arrayOf("BridgeBuilders ${realm.realmId.id}")

        // Создание контента для лобби
        TopManager()
        CustomizationNPC()
        Npcs.init(bridgeBuildersInstance)

        B.events(
            GlobalListeners,
            LobbyHandler,
            Lootbox
        )

        // NPC поиска игры
        val balancer = PlayerBalancer()
        var fixDoubleClick: Player? = null

        worldMeta.getLabels("play").forEach { npcLabel ->
            val npcArgs = npcLabel.tag.split(" ")
            npcLabel.setYaw(npcArgs[0].toFloat())
            npcLabel.setPitch(npcArgs[1].toFloat())
            Npcs.spawn(
                Npc.builder()
                    .location(npcLabel.clone().add(0.5, 0.0, 0.5))
                    .name("§e§lНАЖМИТЕ ЧТОБЫ ИГРАТЬ")
                    .behaviour(NpcBehaviour.STARE_AT_PLAYER)
                    .skinUrl("https://webdata.c7x.dev/textures/skin/$SKIN")
                    .skinDigest("bf30a1df-85de-11e8-a6de-1cb72caa35fd")
                    .type(EntityType.PLAYER)
                    .onClick {
                        if (fixDoubleClick != null && fixDoubleClick == it)
                            return@onClick
                        balancer.accept(it)
                        fixDoubleClick = it
                    }.build()
            )
            val textDataName = UUID.randomUUID().toString()
            IRenderService.get().createGlobalWorldRenderData(
                worldMeta.world.uid,
                textDataName,
                WorldRenderData.builder().visibilityTarget(VisibilityTarget.BLACKLIST).name(textDataName).dataDrawData(
                    DataDrawData.builder()
                        .strings(
                            listOf(
                                StringDrawData.builder().align(1).scale(2).position(V2(100.0, 0.0))
                                    .string("㗬㗬㗬")
                                    .build(),
                                StringDrawData.builder().align(1).scale(2).position(V2(115.0, 40.0))
                                    .string("§l> §dBridgeBuilders §f§l<").build()
                            )
                        ).dimensions(V2(0.0, 0.0))
                        .scale(2.0)
                        .position(V3(npcLabel.x + 2.6, npcLabel.y + 4, npcLabel.z + 0.5))
                        .rotation(180)
                        .build()
                ).build()
            )
            IRenderService.get().setRenderVisible(worldMeta.world.uid, textDataName, true)
        }

        // Команда выхода в хаб
        B.regCommand({ player, _ ->
            Cristalix.transfer(listOf(player.uniqueId), RealmId.of(HUB))
            null
        }, "leave")
    }

    private fun getUser(player: Player) = userManager.getUser(player.uniqueId)

    private fun getUser(uuid: UUID) = userManager.getUser(uuid)
}