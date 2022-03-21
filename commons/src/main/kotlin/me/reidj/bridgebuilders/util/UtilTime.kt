package me.reidj.bridgebuilders.util

/**
 * @project BridgeBuilders
 * @author Рейдж
 */
object UtilTime {

    fun formatTime(time: Long, string: Boolean): String? {
        val hours = time / 3600000
        val minutes = time % 3600000 / 60000
        val seconds = Math.max(time % 3600000 % 60000 / 1000, 0)
        return if (string) (if (hours != 0L) "$hours ч. " else "") + (if (minutes != 0L) "$minutes мин. " else "") + "$seconds сек." else numbStr(
            hours
        ) + ":" + numbStr(minutes) + ":" + numbStr(seconds)
    }

    private fun numbStr(l: Long): String {
        return if (l >= 10) java.lang.Long.toString(l) else "0$l"
    }
}