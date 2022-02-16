package me.reidj.bridgebuilders.donate

enum class Rare(val title: String, private val color: String) {

    COMMON("Обычный", "§a"),
    RARE("Редкий", "§9"),
    EPIC("Эпический", "§5"),
    LEGENDARY("Легендарный", "§6"),;

    fun with(content: String): String {
        return "${getColored()} §7$content"
    }

    fun getColor(): String {
        return color
    }

    fun getColored(): String {
        return "$color$title"
    }
}