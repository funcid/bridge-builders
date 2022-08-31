package me.reidj.bridgebuilders.donate.impl

import dev.implario.bukkit.item.item
import me.reidj.bridgebuilders.data.Rare
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.user.User
import org.bukkit.Material

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class GraveType(
    private val title: String,
    private val texture: String,
    private val ether: Int,
    private val rare: Rare
) : DonatePosition {
    NONE("Отсутствует", "tochka", 0, Rare.COMMON),
    G1("Поломанная могила", "g1", 64, Rare.COMMON),
    G2("Скромная могила", "g2", 128, Rare.COMMON),
    G3("Монолит", "g3", 256, Rare.RARE),
    G4("Памятник", "g4", 768, Rare.EPIC),
    G5("Монумент", "g5", 2048, Rare.LEGENDARY),
    ;

    override fun getTitle() = title

    override fun getDescription(): String = ""

    override fun getEther() = ether

    override fun getCrystals() = 0

    override fun getLevel() = 0

    override fun getTexture(): String? = null

    override fun getRare() = rare

    override fun getName() = name

    override fun getIcon() = item {
        type(Material.CLAY_BALL)
        nbt("other", texture)
    }

    override fun give(user: User) {
        user.stat.graves.add(name)
    }

    override fun isActive(user: User) = user.stat.currentGrave == name
}