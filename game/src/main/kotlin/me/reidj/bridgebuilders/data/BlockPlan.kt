package me.reidj.bridgebuilders.data

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class BlockPlan(val title: String, val needTotal: Int, val material: Material, val blockData: Byte = 0) {
    COBBLE_WALL("Булыжная ограда", 2, Material.COBBLE_WALL),
    SPRUCE_FENCE("Еловый забор", 4, Material.SPRUCE_FENCE),
    PRISMARINE("Призмарин", 20, Material.PRISMARINE),
    STAINED_CLAY("Фиолетовая керамика", 48, Material.STAINED_CLAY, 10),
    SANDSTONE_STAIRS("Песчаниковые ступеньки", 82, Material.SANDSTONE_STAIRS ),
    CONCRETE("Бирюзовый бетон", 90, Material.CONCRETE, 9),
    COBBLESTONE_STAIRS("Булыжные ступеньки", 118, Material.COBBLESTONE_STAIRS),
    WOOD_STAIRS("Дубовые ступеньки", 164, Material.WOOD_STAIRS),
    WOOD("Еловые доски", 168, Material.WOOD, 1),
    SPRUCE_WOOD_STAIRS("Еловые ступеньки", 232, Material.SPRUCE_WOOD_STAIRS),
    SAND("Песок", 316, Material.SAND),
    STEP("Песчаниковая плита", 317, Material.STEP, 1),
    TRAP_DOOR("Люк", 398, Material.TRAP_DOOR),
    CONCRETE_POWDER("Бирюзовый цемент", 420, Material.CONCRETE_POWDER, 9),
    WOOD_STEP("Еловая плита", 440, Material.WOOD_STEP, 1),
    STONE("Андезит", 820, Material.STONE, 5),
    ;

    fun getItem() = ItemStack(material, 1, 0, blockData)
}