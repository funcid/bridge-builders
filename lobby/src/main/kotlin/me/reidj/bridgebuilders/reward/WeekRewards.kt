package me.reidj.bridgebuilders.reward

import dev.implario.bukkit.item.item
import me.func.mod.data.DailyReward
import me.reidj.bridgebuilders.content.Lootbox
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.impl.Corpse
import me.reidj.bridgebuilders.donate.impl.KillMessage
import me.reidj.bridgebuilders.donate.impl.NameTag
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class WeekRewards(val reward: DailyReward, val give: (User) -> Any) {
    ONE(DailyReward("§e250 монет", item { type = Material.GOLD_INGOT }.build()), { it.giveMoney(250, true) }),
    TWO(DailyReward("§aПрефикс Милашка", NameTag.CUTIE.getIcon()), { it.stat.donates.add(NameTag.CUTIE.getName()) }),
    THREE(
        DailyReward("§9Сообщ. о убийстве - Галактический", ItemStack(Material.ENDER_PEARL)),
        {
            withDuplicate(it, 1000, KillMessage.END) { user, donate ->
                user.stat.donates.contains(donate.getName())
            }
        }
    ),
    FOUR(DailyReward("§5Памятник", Corpse.G4.getIcon()), {
        withDuplicate(it, 2000, Corpse.G4) { user, donate ->
            user.stat.donates.contains(donate.getName())
        }
    }),
    FIVE(
        DailyReward("§bЛутбокс", item { type = Material.CLAY_BALL }.nbt("other", "enderchest1").build()),
        { Lootbox().open(it) }
    ),
    SIX(DailyReward("§e5`000 монет", item {
        type = Material.GOLD_INGOT
        enchant(Enchantment.DAMAGE_ALL, 1)
    }.build()), { it.giveMoney(5000, true) }),
    SEVEN(
        DailyReward("§bЛутбокс §f+ §e5`000 монет", item {
            type = Material.CLAY_BALL
            enchant(Enchantment.DAMAGE_ALL, 1)
        }.nbt("other", "enderchest1").build()),
        {
            Lootbox().open(it)
            it.giveMoney(5000, true)
        }
    ), ;

    companion object {
        fun withDuplicate(user: User, reward: Int, donate: DonatePosition, contains: (User, DonatePosition) -> Boolean) {
            if (contains(user, donate)) user.giveMoney(reward, true)
            else donate.give(user)
        }
    }
}