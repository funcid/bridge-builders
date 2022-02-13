package me.reidj.bridgebuilders.listener

import clepto.bukkit.B
import io.netty.buffer.Unpooled
import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.mod.ModHelper
import me.reidj.bridgebuilders.worldMeta
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import ru.cristalix.core.account.IAccountService
import ru.cristalix.core.display.DisplayChannels
import ru.cristalix.core.display.messages.Mod
import ru.cristalix.core.formatting.Formatting
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.TimeUnit

object GlobalListeners : Listener {

    // Прогрузка файлов модов
    private var modList = try {
        File("./mods/").listFiles()!!
            .map {
                val buffer = Unpooled.buffer()
                buffer.writeBytes(Mod.serialize(Mod(Files.readAllBytes(it.toPath()))))
            }.toList()
    } catch (exception: Exception) {
        Collections.emptyList()
    }

    @EventHandler
    fun PlayerJoinEvent.handle() {
        val user = getByPlayer(player)

        player.sendMessage(Formatting.fine("О найденных багах сообщать в ВК - https://vk.com/reidj.java"))

        B.postpone(5) {player.teleport(worldMeta.getLabel("spawn").clone().add(0.5, 0.0, 0.5))}

        // Отправка модов
        modList.forEach {
            user.sendPacket(
                PacketPlayOutCustomPayload(
                    DisplayChannels.MOD_CHANNEL,
                    PacketDataSerializer(it.retainedSlice())
                )
            )
        }
        ModHelper.updateBalance(user)

        // Заполнение имени для топа
        if (user.stat.lastSeenName == null || (user.stat.lastSeenName != null && user.stat.lastSeenName!!.isEmpty()))
            user.stat.lastSeenName =
                IAccountService.get().getNameByUuid(UUID.fromString(user.session.userId)).get(1, TimeUnit.SECONDS)
    }

    @EventHandler
    fun BlockRedstoneEvent.handle() {
        newCurrent = oldCurrent
    }

    @EventHandler
    fun PlayerInteractEntityEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockFadeEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockSpreadEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun EntityChangeBlockEvent.handle() {
        isCancelled = true
        block.state.update(false, false)
    }

    @EventHandler
    fun BlockGrowEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun BlockPhysicsEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerArmorStandManipulateEvent.handle() {
        isCancelled = true
    }

    @EventHandler
    fun PlayerSwapHandItemsEvent.handle() {
        isCancelled = true
    }
}