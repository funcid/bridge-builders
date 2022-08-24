package me.reidj.node.map

import me.reidj.node.team.Block
import org.bukkit.Material.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class MapType(
    val address: String,
    var blocks: Set<Block>,
    var needBlocks: Int,
    var length: Int,
    var height: Int
) {
    AQUAMARINE(
        "Aquamarine",
        HashSet<Block>(
            listOf(
                Block("Булыжная ограда", 2, COBBLE_WALL),
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
        1762,
        42,
        30
    ),
    BTR(
        "btr",
        HashSet<Block>(
            listOf(
                Block("Фиолетовая керамика", 10, PURPLE_GLAZED_TERRACOTTA),
                Block("Незеритовая плита", 12, STEP, 6),
                Block("Булыжник", 42, COBBLESTONE),
                Block("Тропическая плита", 42, WOOD_STEP, 3),
                Block("Люк", 68, TRAP_DOOR),
                Block("Железные прутья", 90, IRON_FENCE),
                Block("Глина", 102, CLAY),
                Block("Гравий", 106, GRAVEL),
                Block("Булыжная плита", 124, STEP, 3),
                Block("Булыжные ступеньки", 160, COBBLESTONE_STAIRS),
                Block("Светло-серый цемент", 322, CONCRETE_POWDER, 8),
                Block("Андезит", 452, STONE, 5),
            )
        ),
        1530,
        43,
        30
    ),
    COSMOS(
        "Cosmos",
        HashSet<Block>(
            listOf(
                Block("Динамит", 4, TNT),
                Block("Котёл", 4, CAULDRON_ITEM),
                Block("Плита из кирпичей", 20, STEP, 5),
                Block("Андезит", 48, STONE, 5),
                Block("Незеритовая плита", 56, STEP, 6),
                Block("Булыжные ступеньки", 56, COBBLESTONE_STAIRS),
                Block("Булыжная плита", 56, STEP, 3),
                Block("Глина", 68, CLAY),
                Block("Гравий", 112, GRAVEL),
                Block("Светло-серый бетон", 308, CONCRETE, 8),
                Block("Бирюзовая керамика", 336, STAINED_CLAY, 9),
            )
        ),
        1068,
        42,
        30
    ),
    PESOK(
        "pesok",
        HashSet<Block>(
            listOf(
                Block("Табличка", 2, SIGN),
                Block("Кнопка", 14, WOOD_BUTTON),
                Block("Люк", 16, TRAP_DOOR),
                Block("Еловые ступеньки", 16, SPRUCE_WOOD_STAIRS),
                Block("Гранит", 28, STONE, 1),
                Block("Зелёная панель", 42, STAINED_GLASS_PANE, 13),
                Block("Дубовые доски", 42, WOOD),
                Block("Еловая плита", 42, WOOD_STEP, 1),
                Block("Песок", 63, SAND),
                Block("Песчаник", 224, SANDSTONE),
                Block("Терракота", 174, HARD_CLAY),
                Block("Светло-серая керамика", 224, STAINED_CLAY, 8),
            )
        ),
        887,
        42,
        30
    ),
}