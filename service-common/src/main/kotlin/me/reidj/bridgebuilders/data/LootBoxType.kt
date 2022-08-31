package me.reidj.bridgebuilders.data

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class LootBoxType(val lootBox: LootBox) {
    COMMON(LootBox(Rare.COMMON, 0, 0, 192, 5, 3)),
    UNUSUAL(LootBox(Rare.UNUSUAL, 0, 0, 256, 10, 5)),
    RARE(LootBox(Rare.RARE, 0, 0, 512, 20, 7)),
    EPIC(LootBox(Rare.EPIC, 0, 0, 768, 30, 9)),
    LEGENDARY(LootBox(Rare.LEGENDARY, 0, 0, 1024, 50, 11)),
    DONATE(LootBox(Rare.DONATE, 0, 49, 0, 0, 13)),
    ;
}