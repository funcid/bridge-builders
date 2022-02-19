package me.reidj.bridgebuilders.battlepass.quest

import me.reidj.bridgebuilders.battlepass.quest.QuestType.*

object QuestGenerator {

    private fun new(lore: String, aim: Int, type: QuestType) = BattlePassQuest(lore, type, aim)

    private val quests = listOf(
        new("Внесите блоки в строительство моста", 100, POINTS),
        new("Одержите 1 победу", 1, WIN),
        new("Сломайте 1 Маяк", 1, BREAK),
        new("Сыграйте 1 игру", 1, PLAY),
        new("Устраните 4 противников", 4, KILL),
        new("Создайте 30 предметов", 30, CRAFT),
        new("Добудьте Алмазную руду", 15, BREAK),
        new("Добудьте Железную руду", 45, BREAK),
        new("Добудьте Уголь", 64, BREAK),
        new("Создайте Алмазные ботинки", 1, CRAFT),
        new("Создайте Железный нагрудник", 1, CRAFT),
    ).map { it.apply { exp = 25 } }

    private val rare = listOf(
        new("Внесите блоки в строительство моста", 500, POINTS),
        new("Одержите 5 побед", 5, WIN),
        new("Сломайте 8 Маяков", 1, BREAK),
        new("Сыграйте 15 игр", 15, PLAY),
        new("Устраните 15 противников", 15, KILL),
        new("Создайте 100 предметов", 100, CRAFT),
        new("Создайте Алмазный шлем", 2, CRAFT),
        new("Создайте Железные поножи", 1, CRAFT),
    ).map { it.apply { exp = 50 } }

    private val special = listOf(
        new("Внесите блоки в строительство моста", 1000, POINTS),
        new("Одержите 15 побед", 15, WIN),
        new("Сломайте 15 Маяков", 15, BREAK),
        new("Сыграйте 25 игр", 25, PLAY),
        new("Устраните 50 противников", 50, KILL),
        new("Создайте 300 предметов", 300, CRAFT),
        new("Добудьте Железную руду", 150, BREAK),
        new("Создайте Алмазный меч", 2, CRAFT),
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