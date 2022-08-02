package me.reidj.bridgebuilders.achievement

import me.reidj.bridgebuilders.user.User

enum class Achievement(
    val title: String,
    val lore: String,
    val predicate: (User) -> Boolean,
    val reward: (User) -> Any
) {
    BEGINNER("Новичок", "§7Убить 10 игроков\n§f + §b1 лутбокс", { it.stat.kills >= 10 }, { it.stat.lootbox++ }),
    ACCUSTOMED(
        "Освоившийся",
        "§7Убить 25 игроков\n§f + §e64 монеты",
        { it.stat.kills >= 25 },
        { it.giveMoney(64, true) }),
    MERCENARY("Наемник", "§7Убить 50 игроков\n§f + §e128 монет", { it.stat.kills >= 50 }, { it.giveMoney(128, true) }),
    HOPLITE("Гоплит", "§7Убить 100 игроков\n§f + §e128 монет\n + §b1 лутбокс", { it.stat.kills >= 100 }, {
        it.stat.lootbox++
        it.giveMoney(128, true)
    }),
    STRYKER("Страйкер", "§7Убить 250 игроков\n§f + §e256 монет\n + §b1 лутбокс", { it.stat.kills >= 250 }, {
        it.stat.lootbox++
        it.giveMoney(256, true)
    }),
    PRIMARIS("Примарис", "§7Убить 500 игроков\n§f + §e1024 монеты\n + §b3 лутбокса", { it.stat.kills >= 500 }, { user ->
        repeat(3) { user.stat.lootbox++ }
        user.giveMoney(1024, true)
    }),
    CENTURION("Центурион", "§7Убить 1000 игроков\n+ §e2048 монет\n + §b3 лутбокса", { it.stat.kills >= 1000 }, { user ->
        repeat(3) { user.stat.lootbox++ }
        user.giveMoney(2048, true)
    }),
    FIRST_WIN(
        "Победитель Деревянного Ранга",
        "§7Победить 1 раз\n§f + §e32 монеты",
        { it.stat.wins >= 1 },
        { it.giveMoney(32, true) }),
    TEN_WIN("Победитель Медного Ранга", "§7Победить 10 раз\n§f + §e64 монеты\n" +
            "§f + 1 §bЛутбокс", { it.stat.wins >= 10 }, {
        it.giveMoney(64, true)
        it.stat.lootbox++
    }),
    HUNDRED_WIN("Герой", "§7Победить 100 раз\n" +
            "§f + 3 §bЛутбокса\n§f + §e512 монет", { it.stat.wins >= 100 }, { user ->
        repeat(3) { user.stat.lootbox++ }
        user.giveMoney(512, true)
    }),
    THOUSAND_WIN("Легенда", "§7Победить 1`000 раз\n" +
            "§f + 20 §bЛутбоксов\n§f + §e8192 монет", { it.stat.wins >= 1000 }, { user ->
        repeat(20) { user.stat.lootbox++ }
        user.giveMoney(8192, true)
    }),
    GAMER("Игрок", "§7Сыграть 10 игр\n§f + §e128 монет", { it.stat.games >= 10 }, {
        it.giveMoney(128, true)
    }),
    GLADIATOR("Гладиатор", "§7Сыграть 100 игр\n§f + §e64 монеты", { it.stat.games >= 100 }, {
        it.giveMoney(64, true)
    }),
    MAXIMUS(
        "Максимус",
        "§7Сыграть 1000 игр\n§f + §e4096 монеты\n§f + 10 §bЛутбоксов",
        { it.stat.games >= 1000 },
        { user ->
            user.giveMoney(4096, true)
            repeat(10) { user.stat.lootbox++ }
        }),
    LOOTER("Счастливчик", "§7Открыть 10 лутбоксов\n§f + 1 §bЛутбокс", { it.stat.lootboxOpenned >= 10 }, { user ->
        user.stat.lootbox++
    }),
    COLLECTROR(
        "Коллекционер",
        "§7Открыть 30 лутбоксов\n§f + 3 §bЛутбокс\n§f + §e512 монет",
        { it.stat.lootboxOpenned >= 30 },
        { user ->
            repeat(3) { user.stat.lootbox++ }
            user.giveMoney(512, true)
        }),
    SEARCH_TREASURE(
        "В поисках сокровищ",
        "§7Открыть 50 лутбоксов\n§f + 6 §bЛутбокс\n§f + §e1024 монет",
        { it.stat.lootboxOpenned >= 50 },
        { user ->
            repeat(5) { user.stat.lootbox++ }
            user.giveMoney(1024, true)
        }),
    REIDJ_GIFTS(
        "Дары Рейджа",
        "§7Открыть 100 лутбоксов\n§f + 10 §bЛутбокс\n§f + §e2048 монет",
        { it.stat.lootboxOpenned >= 100 },
        { user ->
            repeat(10) { user.stat.lootbox++ }
            user.giveMoney(2048, true)
        }),
    NOT_TOUCH(
        "Руками не трогать",
        "§7Открыть 150 лутбоксов\n§f + 15 §bЛутбокс\n§f + §e4096 монет",
        { it.stat.lootboxOpenned >= 150 },
        { user ->
            repeat(15) { user.stat.lootbox++ }
            user.giveMoney(4096, true)
        }),
    CURIOSITY(
        "Любопытство",
        "§7Открыть 250 лутбоксов\n§f + 20 §bЛутбоксов\n§f + §e8192 монет",
        { it.stat.lootboxOpenned >= 250 },
        { user ->
            repeat(20) { user.stat.lootbox++ }
            user.giveMoney(8192, true)
        }),
}