package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import dev.implario.bukkit.item.item
import dev.xdark.feder.GlobalSerializers
import me.func.protocol.packet.PackageWrapper
import me.reidj.bridgebuilders.Status
import me.reidj.bridgebuilders.activeStatus
import me.reidj.bridgebuilders.packet_handler.CameraManager
import me.reidj.bridgebuilders.teams
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import ru.cristalix.core.item.Items
import java.io.File

object ConnectionHandler : Listener {

    val manager: CameraManager = CameraManager
    var target: Player? = null

    private val back = item {
        type = Material.CLAY_BALL
        nbt("other", "cancel")
        text("§cВернуться")
    }.build()

    init {
        B.regCommand({ _, args ->
            target = Bukkit.getPlayer(args[0])
            "Съёмка начата"
        }, "start")

        B.regCommand({ _, _ ->
            val file = File("movie-${100 + (Math.random() * 900).toInt()}.txt")
            file.writeText(
                manager.actions.toTypedArray().joinToString {
                    GlobalSerializers.toJson(
                        PackageWrapper(it::class.java.name, GlobalSerializers.toJson(it))
                    )
                })
            file.createNewFile()
            manager.actions.clear()
            "Сохранение в файл"
        }, "save")
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        player.inventory.clear()
        player.gameMode = GameMode.ADVENTURE

        /*repeat(300) {
            player.world.spawn(player.location, Pig::class.java)
        }

        player.teleport(Location(player.world, -8.0, 2.0, -2.0))
        player.allowFlight = true
        player.isFlying = true
        player.gameMode = GameMode.CREATIVE
        B.postpone(100) { View() }*/

        /*(player as CraftPlayer).handle.playerConnection.networkManager.channel.pipeline()
            .addBefore("packet_handler", player.name, object :
                ChannelDuplexHandler() {
                override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
                    if (target != null && player != target) {
                        when (msg) {
                            is PacketPlayOutBlockChange -> manager.actions.add(msg)
                            is PacketPlayOutMultiBlockChange -> manager.actions.add(msg)
                            is PacketPlayOutMultiPacket -> manager.actions.add(msg)
                        }
                    }
                    super.write(ctx, msg, promise)
                }
            })*/

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
}