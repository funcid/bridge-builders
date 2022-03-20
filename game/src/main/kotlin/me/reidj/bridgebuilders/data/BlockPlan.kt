package me.reidj.bridgebuilders.data

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class BlockPlan(val title: String, val needTotal: Int, val material: Material, val blockData: Byte = 0) {
    COBBLE_WALL("Булыжная ограда", 1, Material.COBBLE_WALL),
    SPRUCE_FENCE("Еловый забор", 2, Material.SPRUCE_FENCE),
    PRISMARINE("Призмарин", 13, Material.PRISMARINE),
    STAINED_CLAY("Фиолетовая керамика", 32, Material.STAINED_CLAY, 10),
    SANDSTONE_STAIRS("Песчаниковые ступеньки", 42, Material.SANDSTONE_STAIRS),
    CONCRETE("Бирюзовый бетон", 60, Material.CONCRETE, 9),
    COBBLESTONE_STAIRS("Булыжные ступеньки", 74, Material.COBBLESTONE_STAIRS),
    WOOD_STAIRS("Дубовые ступеньки", 100, Material.WOOD_STAIRS),
    WOOD("Еловые доски", 88, Material.WOOD, 1),
    SPRUCE_WOOD_STAIRS("Еловые ступеньки", 136, Material.SPRUCE_WOOD_STAIRS),
    SAND("Песок", 180, Material.SAND),
    STEP("Песчаниковая плита", 167, Material.STEP, 1),
    TRAP_DOOR("Люк", 200, Material.TRAP_DOOR),
    CONCRETE_POWDER("Бирюзовый цемент", 258, Material.CONCRETE_POWDER, 9),
    WOOD_STEP("Еловая плита", 272, Material.WOOD_STEP, 1),
    STONE("Андезит", 521, Material.STONE, 5),
    ;

    fun getItem() = ItemStack(material, 1, 0, blockData)
}