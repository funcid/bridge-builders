package me.reidj.bridgebuilders.donate.impl

import me.reidj.bridgebuilders.data.Rare
import me.reidj.bridgebuilders.data.Rare.*
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material
import org.bukkit.Material.*
import org.bukkit.inventory.ItemStack

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class WalkingEffectType(
    private val title: String,
    private val ether: Int,
    private val crystals: Int,
    private val rare: Rare,
    private val icon: Material
) : DonatePosition {
    NONE("Отсутствует", 0, 0, COMMON, BARRIER),
    SLIME("Слизь", 512, 0, COMMON, SLIME_BALL),
    DRIP_WATER("Капли воды", 512, 0, COMMON, WATER_BUCKET),
    SPELL_INSTANT("Фейерверк", 1024, 0, RARE, FIREWORK),
    REDSTONE("Красный камень", 1024, 0, RARE, Material.REDSTONE),
    VILLAGER_ANGRY("Злой житель", 2048, 0, EPIC, FIREWORK_CHARGE),
    SPELL_WITCH("Колдунья", 2048, 0, EPIC, NETHER_STALK),
    VILLAGER_HAPPY("Счастливый житель", 2048, 0, EPIC, LIME_GLAZED_TERRACOTTA),
    FLAME("Огонь", 2048, 0, EPIC, FLINT_AND_STEEL),
    LAVA("Лава", 4096, 0, LEGENDARY, LAVA_BUCKET),
    NOTE("Ноты", 4096, 0, LEGENDARY, BOOK),
    HEART("Сердечки", 4096, 0, LEGENDARY, DIAMOND);

    override fun getTitle() = title

    override fun getDescription(): String = ""

    override fun getEther() = ether

    override fun getCrystals() = crystals

    override fun getLevel() = 0

    override fun getName() = name

    override fun getRare() = rare

    override fun getIcon() = ItemStack(icon)

    override fun getTexture(): String? = null

    override fun give(user: User) {
        user.stat.walkingEffects.add(name)
    }

    override fun isActive(user: User): Boolean = user.stat.currentWalkingEffect == name

}