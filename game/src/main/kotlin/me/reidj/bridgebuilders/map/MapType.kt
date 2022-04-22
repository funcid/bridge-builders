package me.reidj.bridgebuilders.map

import me.reidj.bridgebuilders.data.Block
import org.bukkit.Material.*

enum class MapType(
    val title: String,
    var blocks: Set<Block>,
    var needBlocks: Int
) {
    AQUAMARINE(
        "Aquamarine",
        HashSet<Block>(
            listOf(
                Block("Булыжная ограда", 1, COBBLE_WALL),
                Block("Еловый забор", 2, SPRUCE_FENCE),
                Block("Призмарин", 10, PRISMARINE),
                Block("Фиолетовая керамика", 24, STAINED_CLAY, 10),
                Block("Песчаниковые ступеньки", 42, SANDSTONE_STAIRS),
                Block("Бирюзовый бетон", 48, CONCRETE, 9),
                Block("Булыжные ступеньки", 56, COBBLESTONE_STAIRS),
                Block("Дубовые ступеньки", 84, WOOD_STAIRS),
                Block("Еловые доски", 84, WOOD, 1),
                Block("Еловые ступеньки", 116, SPRUCE_WOOD_STAIRS),
                Block("Песчаниковая плита", 130, STEP, 1),
                Block("Песок", 160, SAND),
                Block("Люк", 164, TRAP_DOOR),
                Block("Бирюзовый цемент", 216, CONCRETE_POWDER, 9),
                Block("Еловая плита", 218, WOOD_STEP, 1),
                Block("Андезит", 406, STONE, 5)
            )
        ),
        1761
    ),
    BTR(
        "btr",
        // 123
        HashSet<Block>(
            listOf(
                Block("Фиолетовая керамика", 10, PURPLE_GLAZED_TERRACOTTA),
                Block("Глина", 12, CLAY),
                Block("Гравий", 16, GRAVEL),
                Block("Булыжник", 18, COBBLESTONE),
                Block("Тропическая плита", 41, WOOD_STEP, 3),
                Block("Булыжные ступеньки", 56, COBBLESTONE_STAIRS),
                Block("Люк", 68, TRAP_DOOR),
                Block("Железные прутья", 69, IRON_FENCE),
                Block("Незеритовая плита", 87, STEP, 6),
                Block("Булыжная плита", 123, STEP, 3),
                Block("Светло-серый цемент", 216, CONCRETE_POWDER, 8),
                Block("Андезит", 406, STONE, 5),
            )
        ),
        1122
    )
}