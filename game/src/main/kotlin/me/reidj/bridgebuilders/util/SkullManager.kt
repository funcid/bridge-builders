package me.reidj.bridgebuilders.util

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.server.v1_12_R1.NBTTagCompound
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.*

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
object SkullManager {

    fun create(URL: String?): ItemStack {
        val nms = CraftItemStack.asNMSCopy(ItemStack(Material.SKULL_ITEM, 1, 3.toShort()))
        val skull = nms.asBukkitMirror()
        val skullMeta: SkullMeta = skull.itemMeta as SkullMeta
        val gameProfile = GameProfile(UUID.randomUUID(), "")

        skullMeta.displayName = "§aBridgeBuilders"
        skullMeta.lore = listOf(
            "§7Добывай ресурсы, убивай игроков,",
            "§7не давай вражеским командам",
            "§7построить свой мост быстрее",
            "§7твоей команды, чтобы сломать",
            "§7маяк и победить!",
            "REALM:BRIL"
        )

        gameProfile.getProperties().put("textures", Property("textures", URL))
        val profileField: Field

        try {
            profileField = skullMeta::class.java.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField.set(skullMeta, gameProfile)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        skull.itemMeta = skullMeta

        return skull
    }
}