package me.reidj.lobby.content

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.impl.GraveType
import me.reidj.bridgebuilders.donate.impl.MessageType
import me.reidj.bridgebuilders.donate.impl.NameTagType
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class WeekRewards(val title: String, val icon: ItemStack, val give: (User) -> Any) {
    ONE("§e250 Эфира", item { type = org.bukkit.Material.GOLD_INGOT }, { it.giveEther(250) }),
    TWO("§aПрефикс Мостовой", NameTagType.BRIDGE.getIcon(), { it.stat.nameTags.add(NameTagType.BRIDGE.name) }),
    THREE(
        "§9Сообщ. о убийстве - Галактический", ItemStack(Material.ENDER_PEARL),
        {
            withDuplicate(it, 1000, MessageType.GALACTIC) { user, donate ->
                donate.getName() in user.stat.messages
            }
        }
    ),
    FOUR("§5Памятник", GraveType.G4.getIcon(), {
        withDuplicate(it, 1500, GraveType.G4) { user, donate ->
            donate.getName() in user.stat.graves
        }
    }),
    FIVE(
        "§9Редкий лутбокс", item {
            type = Material.CLAY_BALL
            nbt("other", "enderchest1")
        },
        { me.reidj.lobby.app.lootBox.open(it, it.cachedPlayer, me.reidj.bridgebuilders.data.LootBoxType.RARE) }
    ),
    SIX("§e5`000 Эфира", item {
        type = Material.GOLD_INGOT
        enchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 1)
    }, { it.giveEther(5000) }),
    SEVEN(
        "§6Легендарный лутбокс §f+ §e5`000 Эфира", item {
            type = Material.CLAY_BALL
            enchant(org.bukkit.enchantments.Enchantment.DAMAGE_ALL, 1)
            nbt("other", "enderchest1")
        },
        {
            me.reidj.lobby.app.lootBox.open(it, it.cachedPlayer, me.reidj.bridgebuilders.data.LootBoxType.LEGENDARY)
            it.giveEther(5000)
        }
    ), ;

    companion object {
        fun withDuplicate(user: User, reward: Int, donate: DonatePosition, contains: (User, DonatePosition) -> Boolean) {
            if (contains(user, donate)) user.giveEther(reward) else donate.give(user)
        }
    }
}