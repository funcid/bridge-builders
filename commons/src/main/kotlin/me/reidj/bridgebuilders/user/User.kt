package me.reidj.bridgebuilders.user

import dev.implario.kensuke.KensukeSession
import dev.implario.kensuke.impl.bukkit.IBukkitKensukeUser
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PlayerConnection
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.util.*

class User(session: KensukeSession, stat: Stat?) : IBukkitKensukeUser {

    var stat: Stat
    var collectedBlocks = 0
    var activeHand = false

    private var connection: PlayerConnection? = null

    private var session: KensukeSession
    override fun getSession() = session

    override fun getPlayer() = player

    private var player: Player? = null
    override fun setPlayer(p0: Player?) {
        if (p0 != null) {
            player = p0
        }
    }

    init {
        this.stat = stat ?: Stat(
            UUID.fromString(session.userId),
            0,
            0,
            ""
        )
        this.session = session
    }

    fun sendPacket(packet: Packet<*>) {
        if (connection == null)
            connection = (player as CraftPlayer).handle.playerConnection
        connection?.sendPacket(packet)
    }
}