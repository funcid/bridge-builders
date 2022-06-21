package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.donate.MoneyFormatter
import me.reidj.bridgebuilders.donate.Rare
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack

enum class StepParticle(
    private val title: String,
    private val price: Int,
    private val rare: Rare,
    val type: Particle,
    private val icon: Material
) : DonatePosition {
    NONE("Отсутствует", 0, Rare.COMMON, Particle.SPELL_INSTANT, Material.BARRIER),
    SLIME("Слизь", 192, Rare.COMMON, Particle.SLIME, Material.SLIME_BALL),
    WATER_DROP("Капли воды", 256, Rare.COMMON, Particle.DRIP_WATER, Material.WATER_BUCKET),
    SPELL_INSTANT("Фейерверк", 512, Rare.RARE, Particle.SPELL_INSTANT, Material.FIREWORK),
    REDSTONE("Красный камень", 768, Rare.RARE, Particle.REDSTONE, Material.REDSTONE),
    VILLAGER_ANGRY("Злой житель", 1024, Rare.EPIC, Particle.VILLAGER_ANGRY, Material.FIREWORK_CHARGE),
    SPELL_WITCH("Колдунья", 2048, Rare.EPIC, Particle.SPELL_WITCH, Material.NETHER_STALK),
    VILLAGER_HAPPY("Счастливый житель", 2048, Rare.EPIC, Particle.VILLAGER_HAPPY, Material.LIME_GLAZED_TERRACOTTA),
    FLAME("Огонь", 2048, Rare.EPIC, Particle.FLAME, Material.FLINT_AND_STEEL),
    LAVA("Лава", 4096, Rare.LEGENDARY, Particle.LAVA, Material.LAVA_BUCKET),
    NOTE("Ноты", 4096, Rare.LEGENDARY, Particle.NOTE, Material.BOOK),
    HEAR("Сердечки", 4096, Rare.LEGENDARY, Particle.HEART, Material.DIAMOND);

    override fun getTitle(): String {
        return title
    }

    override fun getDescription(): String = ""

    override fun getPrice(): Int {
        return price
    }

    override fun getRare(): Rare {
        return rare
    }

    override fun getIcon(): ItemStack {
        return item {
            type = icon
            text(rare.with("эффект ходьбы $title") + "\n\n§fРедкость: ${rare.getColored()}\n§fСтоимость: ${MoneyFormatter.texted(price)}")
        }.build()
    }

    override fun give(user: User) {
        user.stat.donates.add(getName())
    }

    override fun isActive(user: User): Boolean {
        return user.stat.activeParticle == data.StepParticle.valueOf(name)
    }

    override fun getName(): String {
        return name
    }

    override fun getObjectName(): String = name
}