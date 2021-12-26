package me.reidj.bridgebuilders.packet_handler

import clepto.bukkit.B
import dev.xdark.feder.GlobalSerializers
import me.func.protocol.packet.PackageWrapper
import net.minecraft.server.v1_12_R1.Packet
import net.minecraft.server.v1_12_R1.PacketPlayOutBlockChange
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityEquipment
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.io.File

class View {

    private val movie = String(File("/home/func/forest_new/realms/bri-1/movie-241.txt").readBytes()).split(", ")
        .map { GlobalSerializers.fromJson(it, PackageWrapper::class.java) }
        .map { GlobalSerializers.fromJson(it.objectData, Class.forName(it.clazz)) }
        .map {
            if (it is ItemChangePacket) PacketPlayOutEntityEquipment(
                it.entityId,
                it.slot,
                CraftItemStack.asNMSCopy(ItemStack(it.id))
            ) else it
        }

    init {
        /*val npc = Npc.npc {
            x = 12.0
            y = 92.0
            z = -153.0
            onClick { it.player.sendMessage("123") }
            behaviour = NpcBehaviour.STARE_AT_PLAYER
            name = "Skas"
            id = 383

            skinDigest = "4a9df40e-e0ca-11e8-8374-1cb72caa35fd"
            skinUrl = "https://webdata.c7x.dev/textures/skin/4a9df40e-e0ca-11e8-8374-1cb72caa35fd"
        }.spawn()
        npc.slot(
            EquipmentSlot.HAND,
            CraftItemStack.asNMSCopy(ItemStack(Material.WOOL))
        )
            .slot(
                EquipmentSlot.HEAD,
                CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_HELMET))
            ).slot(
                EquipmentSlot.OFF_HAND,
                CraftItemStack.asNMSCopy(ItemStack(Material.DIRT))
            ).slot(
                EquipmentSlot.CHEST,
                CraftItemStack.asNMSCopy(ItemStack(Material.CHAINMAIL_CHESTPLATE))
            ).slot(
                EquipmentSlot.FEET,
                CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_BOOTS))
            ).slot(
                EquipmentSlot.LEGS,
                CraftItemStack.asNMSCopy(ItemStack(Material.LEATHER_LEGGINGS))
            )
        B.postpone(40) {
            Bukkit.getOnlinePlayers().forEach { player ->
                (player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutEntityTeleport().apply {
                    a = 383
                    b = 20.0
                    c = 92.0
                    d = -153.0
                    e = 0
                    f = 0
                    g = false
                })
            }*/
        movie.forEachIndexed { index, it ->
            B.postpone(index) {
                Bukkit.getOnlinePlayers().forEach { player ->
                    (player as CraftPlayer).handle.playerConnection.sendPacket(it as Packet<*>)
                    if (it is PacketPlayOutBlockChange) {
                        B.bc("" + it.a.x + " " + it.a.y + " " + it.a.z)
                        player.world.getBlockAt(it.a.x, it.a.y, it.a.z).setTypeAndDataFast(it.block.block.id, 0)
                    }
                }
            }
        }
    }
}