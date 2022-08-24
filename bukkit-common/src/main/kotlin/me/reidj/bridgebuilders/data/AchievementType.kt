package me.reidj.bridgebuilders.data

import me.reidj.bridgebuilders.user.User

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class AchievementType(
    val title: String,
    val lore: String,
    val predicate: (User) -> Boolean,
    val reward: (User) -> Any
) {
    // Достижения за победы
    NEWBIE("Новичок", "Победить 1 раз\n§f+ 128 Эфира\n+ 100 опыта\n+ Обычный лутбокс х1", { it.stat.wins >= 1 }, {
        it.giveEther(128)
        it.giveExperience(100)

    }),
    RESPONSIBLE("Освоившийся", "Победить 10 раз\n§f256 Эфира\n+ 250 опыта", { it.stat.wins >= 10 }, {
        it.giveEther(256)
        it.giveExperience(250)

    }),
    EXPERIENCED(
        "Опытный",
        "Победить 25 раз\n§f+ 512 Эфира\n+ 500 опыта\n+ Обычный лутбокс х3\n+ Необычный лутбокс х2",
        { it.stat.wins >= 25 },
        {
            it.giveEther(512)
            it.giveExperience(500)

        }),
    VETERAN("Ветеран", "Победить 50 раз\n§f+1,024 Эфира\n+ 1,000 опыта", { it.stat.wins >= 50 }, {
        it.giveEther(1024)
        it.giveExperience(1000)

    }),
    MASTER(
        "Мастер",
        "Победить 100 раз\n§f+2,048 Эфира\n+ 1,500 опыта\n+ Обычный лутбокс х5\n+ Редкий лутбокс х2",
        { it.stat.wins >= 100 },
        {
            it.giveEther(2048)
            it.giveExperience(1500)

        }),
    LIFE_LEGEND(
        "Живая Легенда",
        "Победить 175 раз\n§f+ 4,096 Эфира\n+ 2,350 опыта\n+ Эпический лутбокс х1",
        { it.stat.wins >= 175 },
        {
            it.giveEther(4096)
            it.giveExperience(2350)

        }),
    LEGENDARY_BUILDER(
        "Легендарный строитель",
        "Победить 300 раз\n§f+ 8,196 Эфира\n+ 4,000 опыта\n+ Легендарный лутбокс х2\n+ Эпический лутбокс х3\n+ Префикс Легендарный строитель",
        { it.stat.wins >= 300 },
        {
            it.giveEther(8196)
            it.giveExperience(4000)

        }),

    // Достижения за убийства
    FIRST_BLOOD(
        "Первая кровь",
        "Убить 1 игрока\n§f+ 128 Эфира\n+ 100 опыта\n+ Обычный лутбокс х1",
        { it.stat.kills >= 1 },
        {
            it.giveEther(128)
            it.giveExperience(100)

        }),
    MURDER("Маньяк", "Убить 10 игроков\n§f+ 256 Эфира\n+ 250 опыта", { it.stat.kills >= 10 }, {
        it.giveEther(256)
        it.giveExperience(250)

    }),
    HITMAN(
        "Наёмный убийца",
        "Убить 25 игроков\n§f+ 512 Эфира\n+ 400 опыта\n+ Необычный лутбокс х3",
        { it.stat.kills >= 25 }, {
            it.giveEther(512)
            it.giveExperience(400)

        }
    ),
    RIPPER("Потрошитель", "Убить 50 игроков\n§f+ 768 Эфира\n+ 700 опыта", { it.stat.kills >= 50 }, {
        it.giveEther(768)
        it.giveExperience(700)

    }),
    PSYCHOPATH(
        "Психопат",
        "Убить 100 игроков\n§f+ 1,024 Эфира\n+ 1,000 опыта\n+ Редкий лутбокс х3",
        { it.stat.kills >= 100 },
        {
            it.giveEther(1024)
            it.giveExperience(1000)

        }),
    SERIAL_MURDER(
        "Серийный маньяк",
        "Убить 250 игроков\n§f+ 2,048 Эфира\n+ 150 опыта\n+ Эпическй лутбокс х2",
        { it.stat.kills >= 250 }, {
            it.giveEther(2048)
            it.giveExperience(150)

        }),
    EXECUTIONER(
        "Палач",
        "Убить 500 игроков\n§f+ 4,096 Эфира\n+ 2,000 опыта\n+ Легендарный лутбокс х1\n+ Эпический лутбокс х3",
        { it.stat.kills >= 500 }, {
            it.giveEther(4096)
            it.giveExperience(2000)

        }),
    JASON_VOORHEES(
        "Джейсон Вурхиз",
        "Убить 1000 игроков\n§f+ 8,192 Эфира\n+ 2,700 опыта\n+ Легендарный лутбокс х2\nЭпический лутбокс х5",
        { it.stat.kills >= 1000 }, {
            it.giveEther(8192)
            it.giveExperience(2700)

        }),
    MINION_OF_CHAOS(
        "Приспешник Хаоса",
        "Убить 2500 игроков\n§f+ 10,256 Эфира\n+ 4,000 опыта\n+ Легендарный лутбокс х5\n+ Эпический лутбокс х10",
        { it.stat.kills >= 2500 }, {
            it.giveEther(10256)
            it.giveExperience(4000)

        }),
    JOHN_WICK(
        "Джон Уик",
        "Убить 5000 игроков\n§f+ 20,540 Эфира\n+ 7,000 опыта\n+ Донат лутбокс х3\n+ Легендарный лутбокс х2",
        { it.stat.kills >= 5000 }, {
            it.giveEther(20540)
            it.giveExperience(7000)

        }),

    // Достижения за уровень
    WOOD_GRADE(
        "Авантюрист Деревянного ранга",
        "Получить 5 уровень\n§f+ 250 Эфира\n+ 90 опыта\nОбычный лутбокс х1",
        { it.getLevel() >= 5 }, {
            it.giveEther(250)
            it.giveExperience(90)

        }),
    COPPER_GRADE(
        "Авантюрист Медного ранга",
        "Получить 15 уровень\n§f+ 512 Эфира\n+ 180 опыта",
        { it.getLevel() >= 15 }, {
            it.giveEther(512)
            it.giveExperience(180)

        }),
    BRONZE_GRADE(
        "Авантюрист Бронзового ранга",
        "Получить 30 уровень\n§f+ 1,024 Эфира\n+ 300 опыта\n+ Необычный лутбокс х3",
        { it.getLevel() >= 30 }, {
            it.giveEther(1024)
            it.giveExperience(300)

        }),
    IRON_GRADE(
        "Авантюрист Железного ранга",
        "Получить 50 уровень\n§f+ 1,526 Эфира\n+ 600 опыта",
        { it.getLevel() >= 50 }, {
            it.giveEther(1526)
            it.giveExperience(600)

        }),
    STEEL_GRADE(
        "Авантюрист Стального ранга",
        "Получить 75 уровень\n§f+ 2,048 Эфира\n+ 1,000 опыта\n+ Редкий лутбокс х3",
        { it.getLevel() >= 75 }, {
            it.giveEther(2048)
            it.giveExperience(1000)

        }),
    GOLD_GRADE(
        "Авантюрист Золотого Ранга",
        "Получить 100 уровень\n§f+ 4,096 Эфира\n+ 1,500 опыта\n+ Эпический лутбокс х1\n+ Редкий лутбокс х3",
        { it.getLevel() >= 100 }, {
            it.giveEther(4096)
            it.giveExperience(1500)

        }),
    CARBON_GRADE(
        "Авантюрист Карбонового Ранга",
        "Получить 125 уровень\n§f+ 5,096 Эфира\n+ 2,000 опыта\n+ Эпический лутбокс х3",
        { it.getLevel() >= 125 }, {
            it.giveEther(5096)
            it.giveExperience(2000)

        }),
    TITANIUM_GRADE(
        "Авантюрист Титанового Ранга",
        "Получить 150 уровень\n§f+ 6,096 Эфира\n+ 2,700 опыта\n+ Префикс Титан\n+ Легендарный лутбокс х2\nЭпический лутбокс х3",
        { it.getLevel() >= 150 }, {
            it.giveEther(6096)
            it.giveExperience(2700)

        }),
    ADAMANTITE_GRADE(
        "Авантюрист Адамантитового Ранга",
        "Получить 300 уровень\n§f+ 10,256 Эфира\n+ 4,000 опыта\n+ Префикс Адамантий\n+ Легендарный лутбокс х5\n+ Эпический лутбокс х10\n+ Донат лутбокс х1",
        { it.getLevel() >= 300 }, {
            it.giveEther(10256)
            it.giveExperience(4000)

        }),
    PLATINUM_GRADE(
        "Авантюрист Платинового Ранга",
        "Получить 500 уровень\n§f+ 20,512 Эфира\n+ 6,000 опыта\n+ Префикс Платина\n+ Легендарный лутбокс х10\n+ Эпический лутбокс х15\n+ Донат лутбокс х3",
        { it.getLevel() >= 500 }, {
            it.giveEther(20512)
            it.giveExperience(6000)

        }),

    // Достижения за открытые лутбоксы
    DISCOVERER(
        "Первооткрыватель",
        "Открыть первый лутбокс\n§f+ 128 Эфира\n+ 90 опыта",
        { it.stat.lootBoxOpened >= 1 },
        {
            it.giveEther(128)
            it.giveExperience(90)
        }),
    KEY_KEEPER("Ключник", "Открыть 10 лутбоксов\n§f+ 256 Эфира\n+ 170 опыта", { it.stat.lootBoxOpened >= 1 }, {
        it.giveEther(256)
        it.giveExperience(170)
    }),
    GAMBLING("Азартный", "Открыть 25 лутбоксов\n§f+ 512 Эфира\n+ 300 опыта", { it.stat.lootBoxOpened >= 25 }, {
        it.giveEther(512)
        it.giveExperience(300)
    }),
    ROULETTE("Рулетка", "Открыть 50 лутбоксов\n§f+ 1,024 Эфира\n+ 500 опыта", { it.stat.lootBoxOpened >= 50 }, {
        it.giveEther(1024)
        it.giveExperience(500)
    }),
    CASINO(
        "Казино",
        "Открыть 100 лутбоксов\n§f+ 2,048 Эфира\n+ 1,250 опыта\n+ Эпический лутбокс х3",
        { it.stat.lootBoxOpened >= 100 }, {
            it.giveEther(2048)
            it.giveExperience(1250)
        }),
    WHAT(
        "Что я сделал!?",
        "Открыть 250 лутбоксов\n§f+ 4,096 Эфира\n+ 1,700 опыта\n+ Легендарный лутбокс х1\n+ Эпический лутбокс х2",
        { it.stat.lootBoxOpened >= 250 }, {
            it.giveEther(4096)
            it.giveExperience(1700)
        }),
    THE_KNIFE(
        "Выпал нож, я не вру!",
        "Открыть 500 лутбоксов\n§f+ 8,192 Эфира\n+ 3,000 опыта\n+ Префикс Мазеллов\n+ Донат лутбокс х2\n+ Легендарный лутбокс х2",
        { it.stat.lootBoxOpened >= 500 }, {
            it.giveEther(8192)
            it.giveExperience(3000)
        }),
    ;
}