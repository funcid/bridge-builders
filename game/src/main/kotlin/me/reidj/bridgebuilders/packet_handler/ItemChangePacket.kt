package me.reidj.bridgebuilders.packet_handler

import net.minecraft.server.v1_12_R1.EnumItemSlot
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PacketDataSerializer
import net.minecraft.server.v1_12_R1.PacketListenerPlayOut

data class ItemChangePacket(
    var entityId: Int = 0,
    var slot: EnumItemSlot? = null,
    var id: Int = 0
) : Packet<PacketListenerPlayOut> {

    override fun a(p0: PacketDataSerializer?) {
        TODO("Not yet implemented")
    }

    override fun a(p0: PacketListenerPlayOut?) {
        TODO("Not yet implemented")
    }

    override fun b(p0: PacketDataSerializer?) {
        TODO("Not yet implemented")
    }
}