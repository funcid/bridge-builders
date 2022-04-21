package me.reidj.bridgebuilders.map

import me.reidj.bridgebuilders.data.Block
import org.bukkit.Material.*

enum class MapType(val data: MapData) {
    AQUAMARINE(
        MapData(
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
        )
    ),
    BTR(
        MapData(
            "btr",
            HashSet<Block>(
                listOf(
                    Block("Железные прутья", 2, IRON_FENCE),
                    Block("Фиолетовая керамика", 4, PURPLE_GLAZED_TERRACOTTA),
                    Block("Песок", 4, SAND),
                    Block("Глина", 12, CLAY),
                    Block("Светло-серый бетон", 16, CONCRETE, 8),
                    Block("Гравий", 16, GRAVEL),
                    Block("Булыжник", 18, COBBLESTONE),
                    Block("Светло-серая шерсть", 18, WOOL, 8),
                    Block("Железный люк", 27, IRON_TRAPDOOR),
                    Block("Ступеньки из кирпича", 58, SMOOTH_STAIRS),
                    Block("Люк", 68, TRAP_DOOR),
                    Block("Булыжная ограда", 90, COBBLE_WALL),
                    Block("Каменные кирпичи", 96, SMOOTH_BRICK),
                    Block("Булыжные ступеньки", 104, COBBLESTONE_STAIRS),
                    Block("Каменная плита", 181, STEP),
                    Block("Светло-серый цемент", 322, CONCRETE_POWDER, 8),
                    Block("Андезит", 418, STONE),
                )
            ),
            1454
        )
    )
}