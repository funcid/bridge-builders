package me.reidj.bridgebuilders.donate.impl

import me.reidj.bridgebuilders.data.Rare
import me.reidj.bridgebuilders.data.Stat
import me.reidj.bridgebuilders.donate.DonatePosition
import me.reidj.bridgebuilders.user.User
import org.bukkit.inventory.ItemStack

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class LootBoxKit(
    private val title: String,
    private val description: String,
    private val crystal: Int,
    private val give: (Stat) -> Any,
) : DonatePosition {
    NEWBIE("§aНовичок", "§710 Обычных лутбоксов\n§a5 Необычных лутбоксов\n§93 Редких лутбокса", 39, { stat ->
        repeat(10) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.COMMON) }
        repeat(5) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.UNUSUAL) }
        repeat(3) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.RARE) }
    }),
    LUCKY("§bСчастливчик", "§a10 Необычных лутбоксов\n§95 Редких лутбоксов\n§53 Эпических лутбокса", 59, { stat ->
        repeat(10) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.UNUSUAL) }
        repeat(5) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.RARE) }
        repeat(3) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.EPIC) }
    }),
    GAMBLING("§6Азартный", "§910 Редких лутбоксов\n§55 Эпических лутбоксов\n§63 Легендарных лутбокса", 79, { stat ->
        repeat(10) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.RARE) }
        repeat(5) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.EPIC) }
        repeat(3) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.LEGENDARY) }
    }),
    ARISTOCRAT("§cАристократ", "§510 Эпических лутбоксов\n§65 Легендарных лутбоксов\n§c5 Донат лутбоксов", 109, { stat ->
        repeat(10) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.EPIC) }
        repeat(5) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.LEGENDARY) }
        repeat(3) { stat.lootBoxes.add(me.reidj.bridgebuilders.data.LootBoxType.DONATE) }
    })
    ;

    override fun getTitle() = "набор лутбоксов $title"

    override fun getDescription() = description

    override fun getEther() = 0

    override fun getCrystals() = crystal

    override fun getRare() = Rare.DONATE

    override fun getName() = name

    override fun getTexture(): String? = me.reidj.bridgebuilders.getTexture(name.lowercase())

    override fun getIcon(): ItemStack {
        TODO("Not yet implemented")
    }

    override fun give(user: User) {
        give(user.stat)
    }

    override fun isActive(user: User) = false
}