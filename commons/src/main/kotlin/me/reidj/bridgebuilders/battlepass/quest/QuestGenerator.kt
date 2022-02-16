package me.reidj.bridgebuilders.battlepass.quest

import me.reidj.bridgebuilders.battlepass.quest.QuestType.*

object QuestGenerator {

    private fun new(lore: String, aim: Int, type: QuestType) = BattlePassQuest(lore, type, aim)

    private val quests = listOf(
        new("Устраните противников в SkyControl", 3, KILL),
        new("Победить в SkyControl", 1, WIN),
        new("Сыграть пару игр в SkyControl", 2, PLAY),
        new("Нанесите много урона на SkyControl", 30, DAMAGE),
        new("Получите очки команды на SkyControl", 1000, POINTS),
        new("Сыграйте пару минут на SkyControl", 15, TIME),

        new("Убейте экипаж в Among Us", 2, KILL),
        new("Победите в Among Us", 1, WIN),
        new("Выполните задания починки корабля на Among Us", 3, POINTS),
        new("Начните саботаж на Among Us", 1, DAMAGE),
        new("Сыграйте пару игр в Among Us", 2, BREAK),

        new("Продержитесь живым 1 минуту за раунд на TntRun", 1, TIME),
        new("Попадите в ТОП-3 раунда на TntRun", 1, PLACE),
        new("Победите на TntRun", 1, WIN),
        new("Попадите в ТОП-3 раунда без двойного прыжка на TntRun", 1, POINTS),

        new("Устраните противников в Землекопах", 2, KILL),
        new("Победить в Землекопах", 1, WIN),
        new("Сыграть пару игр в Землекопы", 2, PLAY),
        new("Нанесите много урона на Землекопах", 25, DAMAGE),
        new("Выкопайте несколько блоков на Землекопах", 200, BREAK),
        new("Сыграйте пару десятков минут на Землекопах", 20, TIME),

        new("Победите в Столбах", 1, WIN),
        new("Сыграть пару игр в Столбы", 2, PLAY),
        new("Найдите пару блоков на Столбах", 15, BREAK),
        new("Сыграйте пару минут на Столбах", 15, TIME),
        new("Спрячьте пару блоков на Столбах", 50, PLACE),

        new("Победите в Laser Tag", 1, WIN),
        new("Сыграйте пару игр в Laser Tag", 2, PLAY),
        new("Сыграйте пару минут на Laser Tag", 15, TIME),
        new("Убейте пару игроков в Laser Tag", 5, KILL),

        new("Устраните противников в SheepWars", 3, KILL),
        new("Победите в SheepWars", 1, WIN),
        new("Сыграйте пару игр в SheepWars", 2, PLAY),
        new("Нанесите урон на SheepWars", 25, DAMAGE),
        new("Поднимите бонусы в SheepWars", 3, POINTS),
        new("Сыграйте пару минут на SheepWars", 15, TIME),
        new("Принесите овечек на базу в SheepWars", 3, PLACE),

        new("Устраните противников в TheBridge", 5, KILL),
        new("Победите в TheBridge", 1, WIN),
        new("Поиграйте пару игр в TheBridge", 2, PLAY),
        new("Нанесите много урона на TheBridge", 25, DAMAGE),
        new("Забейте несколько голов в TheBridge", 3, POINTS),
        new("Сыграйте пару минут на TheBridge", 20, TIME),

        new("Устраните противников в MurderMystery", 5, KILL),
        new("Победите в MurderMystery", 1, WIN),
        new("Поиграйте пару игр в MurderMystery", 2, PLAY),
        new("Сыграйте пару минут на MurderMystery", 20, TIME),
    ).map { it.apply { exp = 25 } }

    private val rare = listOf(
        new("Устраните противников в SkyControl", 15, KILL),
        new("Победить в SkyControl", 3, WIN),
        new("Сыграть пару игр в SkyControl", 5, PLAY),
        new("Нанесите много урона на SkyControl", 100, DAMAGE),
        new("Получите очки команды на SkyControl", 10000, POINTS),
        new("Сыграйте пару минут на SkyControl", 45, TIME),

        new("Убейте экипаж в Among Us", 4, KILL),
        new("Победите в Among Us", 2, WIN),
        new("Выполните задания починки корабля на Among Us", 10, POINTS),
        new("Начните саботаж на Among Us", 5, DAMAGE),

        new("Победите на TntRun", 3, WIN),
        new("Победите без двойного прыжка на TntRun", 1, DAMAGE),

        new("Победите в Столбах", 3, WIN),
        new("Сыграйте много игр в Столбы", 6, PLAY),
        new("Найдите много блоков на Столбах", 100, BREAK),
        new("Сыграйте пол часа на Столбах", 30, TIME),
        new("Спрячьте много блоков на Столбах", 200, PLACE),

        new("Устраните противников в Землекопах", 20, KILL),
        new("Победить в Землекопах", 3, WIN),
        new("Сыграть пару игр в Землекопы", 12, PLAY),
        new("Нанесите много урона на Землекопах", 100, DAMAGE),
        new("Выкопайте несколько блоков на Землекопах", 1000, BREAK),
        new("Сыграйте пару минут на Землекопах", 30, TIME),

        new("Победите в Laser Tag", 3, WIN),
        new("Сыграйте пару игр в Laser Tag", 5, PLAY),
        new("Наиграйте пол часа на Laser Tag", 30, TIME),
        new("Убейте пару игроков в Laser Tag", 25, KILL),

        new("Устраните противников в SheepWars", 10, KILL),
        new("Победите в SheepWars", 3, WIN),
        new("Сыграйте пару игр в SheepWars", 5, PLAY),
        new("Нанесите много урона на SheepWars", 100, DAMAGE),
        new("Поднимите бонусы в SheepWars", 10, POINTS),
        new("Сыграйте пару минут на SheepWars", 20, TIME),
        new("Принесите овечек на базу в SheepWars", 10, PLACE),

        new("Устраните противников в TheBridge", 20, KILL),
        new("Победите в TheBridge", 3, WIN),
        new("Поиграйте пару игр в TheBridge", 5, PLAY),
        new("Нанесите много урона на TheBridge", 70, DAMAGE),
        new("Забейте несколько голов в TheBridge", 6, POINTS),
        new("Сыграйте пару минут на TheBridge", 20, TIME),

        new("Устраните много противников в MurderMystery", 12, KILL),
        new("Победите много игр в MurderMystery", 10, WIN),
        new("Поиграйте множество игр в MurderMystery", 15, PLAY),
        new("Сыграйте пару минут на MurderMystery", 30, TIME),
    ).map { it.apply { exp = 50 } }

    private val special = listOf(
        new("Устраните противников в SkyControl", 100, KILL),
        new("Победить в SkyControl", 10, WIN),
        new("Сыграть пару игр в SkyControl", 50, PLAY),
        new("Нанесите много урона на SkyControl", 200, DAMAGE),
        new("Получите очки команды на SkyControl", 25000, POINTS),
        new("Сыграйте пару минут на SkyControl", 60, TIME),

        new("Убейте экипаж в Among Us", 10, KILL),
        new("Победите в Among Us", 5, WIN),
        new("Выполните задания починки корабля на Among Us", 20, POINTS),
        new("Начните саботаж на Among Us", 7, DAMAGE),
        new("Сыграйте пару игр в Among Us", 7, BREAK),

        new("Отыграйте пару минут на TntRun", 25, TIME),
        new("Победите на TntRun", 10, WIN),
        new("Попадите в ТОП-3 раунда без двойного прыжка на TntRun", 5, POINTS),

        new("Победите в Столбах", 5, WIN),
        new("Сыграть пару игр в Столбы", 20, PLAY),
        new("Найдите блоки на Столбах", 300, BREAK),
        new("Сыграйте один час на Столбах", 60, TIME),

        new("Устраните противников в Землекопах", 50, KILL),
        new("Победить в Землекопах", 5, WIN),
        new("Сыграть пару игр в Землекопы", 35, PLAY),
        new("Нанесите много урона на Землекопах", 200, DAMAGE),
        new("Выкопайте несколько блоков на Землекопах", 5000, BREAK),
        new("Сыграйте пару минут на Землекопах", 60, TIME),

        new("Победите в Laser Tag", 10, WIN),
        new("Сыграть много игр в Laser Tag", 40, PLAY),
        new("Поиграйте час на Laser Tag", 60, TIME),
        new("Убейте много игроков в Laser Tag", 100, KILL),

        new("Устраните противников в SheepWars", 20, KILL),
        new("Победите в SheepWars", 5, WIN),
        new("Нанесите много урона на SheepWars", 150, DAMAGE),
        new("Поднимите бонусы в SheepWars", 15, POINTS),
        new("Сыграйте пару минут на SheepWars", 30, TIME),
        new("Принесите овечек на базу в SheepWars", 20, PLACE),

        new("Устраните противников в TheBridge", 30, KILL),
        new("Победите в TheBridge", 10, WIN),
        new("Поиграйте пару игр в TheBridge", 15, PLAY),
        new("Нанесите много урона на TheBridge", 150, DAMAGE),
        new("Забейте несколько голов в TheBridge", 9, POINTS),
        new("Сыграйте пару минут на TheBridge", 30, TIME),

        new("Устраните множество противников в MurderMystery", 20, KILL),
        new("Победите множество игр в MurderMystery", 20, WIN),
        new("Наиграйте очень много игр в MurderMystery", 30, PLAY),
        new("Наиграйте час на MurderMystery", 60, TIME),
    ).map { it.apply { exp = 100 } }

    fun generate() = listOf(
        quests.random(),
        quests.random(),
        quests.random(),
        rare.random(),
        rare.random(),
        special.random(),
        quests.random(),
        rare.random(),
    )

}