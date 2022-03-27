package me.reidj.bridgebuilders.data

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class BlockPlan(val title: String, val needTotal: Int, val material: Material, val blockData: Byte = 0) {
    COBBLE_WALL("Булыжная ограда", 1, Material.COBBLE_WALL),
    SPRUCE_FENCE("Еловый забор", 2, Material.SPRUCE_FENCE),
    PRISMARINE("Призмарин", 10, Material.PRISMARINE),
    STAINED_CLAY("Фиолетовая керамика", 24, Material.STAINED_CLAY, 10),
    SANDSTONE_STAIRS("Песчаниковые ступеньки", 42, Material.SANDSTONE_STAIRS),
    CONCRETE("Бирюзовый бетон", 48, Material.CONCRETE, 9),
    COBBLESTONE_STAIRS("Булыжные ступеньки", 56, Material.COBBLESTONE_STAIRS),
    WOOD_STAIRS("Дубовые ступеньки", 84, Material.WOOD_STAIRS),
    WOOD("Еловые доски", 84, Material.WOOD, 1),
    SPRUCE_WOOD_STAIRS("Еловые ступеньки", 116, Material.SPRUCE_WOOD_STAIRS),
    SAND("Песок", 160, Material.SAND),
    STEP("Песчаниковая плита", 130, Material.STEP, 1),
    TRAP_DOOR("Люк", 164, Material.TRAP_DOOR),
    CONCRETE_POWDER("Бирюзовый цемент", 216, Material.CONCRETE_POWDER, 9),
    WOOD_STEP("Еловая плита", 218, Material.WOOD_STEP, 1),
    STONE("Андезит", 406, Material.STONE, 5),
    ;

    fun getItem() = ItemStack(material, 1, 0, blockData)
}