package me.reidj.bridgebuilders.donate.impl

import me.reidj.bridgebuilders.data.Rare
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.user.User
import org.bukkit.inventory.ItemStack

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class MoneyKitType(
    private val title: String,
    private val crystal: Int,
    private val reward: Int,
) : DonatePosition {
    HANDFUL_OF_COINS("Горсть Эфира", 29, 512),
    BAG_OF_COINS("Мешок с Эфиром", 39, 1024),
    CHEST_WITH_COINS("Сундук с Эфиром", 59, 2048),
    TREASURY("Скоровищница c Эфиром", 79, 4096),
    //SAFE("Сейф с Эфиром", 109, 8192),
    ;

    override fun getTitle() = title

    override fun getDescription() = "Вы получите §d$reward Эфира"

    override fun getEther() = 0

    override fun getCrystals() = crystal

    override fun getRare() = Rare.DONATE

    override fun getName() = name

    override fun getTexture(): String? = me.reidj.bridgebuilders.getTexture(name.lowercase())

    override fun getIcon(): ItemStack {
        TODO("Not yet implemented")
    }

    override fun getLevel() = 0

    override fun give(user: User) {
        user.giveEther(reward)
    }

    override fun isActive(user: User) = false
}