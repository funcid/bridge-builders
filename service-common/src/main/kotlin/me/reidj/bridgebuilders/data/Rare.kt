package me.reidj.bridgebuilders.data

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class Rare(private val title: String, private val color: String) {
    COMMON("Обычный", "§7"),
    UNUSUAL("Необычный", "§a"),
    RARE("Редкий", "§9"),
    EPIC("Эпический", "§5"),
    LEGENDARY("Легендарный", "§6"),
    MYTHIC("Мифический", "§d"),
    DONATE("Донат", "§c")
    ;

    fun getTitle() = title

    fun with(content: String) = "${getColored()} §7$content"

    fun getColor() = color

    fun getColored() = "$color$title"
}