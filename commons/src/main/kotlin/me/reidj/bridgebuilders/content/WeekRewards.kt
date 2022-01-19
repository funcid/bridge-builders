package me.reidj.bridgebuilders.content

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.impl.*
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import ru.cristalix.core.formatting.Formatting

enum class WeekRewards(val title: String, val icon: ItemStack, val give: (User) -> Any) {
    ONE("§e32 монеты", ItemStack(Material.GOLD_INGOT), { it.giveMoney(32) }),
    TWO("§b1 Лутбокс", LootboxUnit.getIcon(), { it.stat.lootbox++ }),
    THREE(
        Corpse.G2.getRare().with(Corpse.G2.getTitle()),
        Corpse.G2.getIcon(),
        { giveWithDuplicate(it, Corpse.G2, 64) }
    ),
    FOUR(
        StepParticle.SPELL_INSTANT.getRare().with(StepParticle.SPELL_INSTANT.getTitle()),
        StepParticle.SPELL_INSTANT.getIcon(),
        { giveWithDuplicate(it, StepParticle.SPELL_INSTANT, 128) }
    ),
    FIVE(
        NameTag.NINJA.getRare().with(NameTag.NINJA.getTitle()),
        NameTag.NINJA.getIcon(),
        { giveWithDuplicate(it, NameTag.NINJA, 256) }
    ),
    SIX(
        ArrowParticle.SPELL_INSTANT.getRare().with(ArrowParticle.SPELL_INSTANT.getTitle()),
        ArrowParticle.SPELL_INSTANT.getIcon(),
        { giveWithDuplicate(it, ArrowParticle.SPELL_INSTANT, 512) }
    ),
    SEVEN(
        "§b5 Лутбоксов",
        item {
            type = Material.CLAY_BALL
            enchant(Enchantment.DAMAGE_ALL, 1)
            nbt("other", "enderchest1")
        }, { repeat(5) { _ -> it.stat.lootbox++ } }
    )
    ;

    companion object {
        fun giveWithDuplicate(user: User, donate: DonatePosition, reward: Int) {
            if (user.stat.donate.contains(donate)) {
                user.stat.money += reward
                user.player!!.sendMessage(Formatting.fine("§aДубликат! §fЗаменен на §e$reward монет§f."))
            } else {
                donate.give(user)
            }
        }
    }
}