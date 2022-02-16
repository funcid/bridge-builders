package me.reidj.bridgebuilders.battlepass

import implario.humanize.Humanize
import me.func.mod.Anime
import me.func.protocol.battlepass.BattlePassUserData
import me.reidj.bridgebuilders.battlepass.quest.QuestGenerator
import me.reidj.bridgebuilders.battlepass.quest.QuestType
import me.reidj.bridgebuilders.getByPlayer
import org.bukkit.entity.Player

const val BATTLEPASS_RECHARGE_HOURS = 6

object BattlePassUtil {

    fun update(player: Player, type: QuestType, value: Int, absolute: Boolean = false) {
        val data = getByPlayer(player).stat
        if (data.progress == null)
            data.progress = BattlePassUserData(15, false)
        if (data.data!!.isEmpty())
            data.data = QuestGenerator.generate()

        data.data?.find { it.questType == type }?.let {
            if (it.goal <= it.now)
                return
            if ((data.data?.indexOf(it) ?: 0) > 5 && data.progress?.advanced != true)
                return

            if (absolute) it.now = value
            else it.now += value

            if (it.goal <= it.now) {
                Anime.topMessage(player, "§lЗадание выполнено! §6Награда: §b${it.exp} опыта §6баттлпасса")
                data.progress!!.exp += it.exp
            }
        }
    }

    fun getQuestLore(player: Player): List<String> {
        val data = getByPlayer(player).stat
        val now = System.currentTimeMillis()

        if (now - data.lastGenerationTime > 1000 * 60 * 60 * BATTLEPASS_RECHARGE_HOURS) {
            data.data = QuestGenerator.generate()
            data.lastGenerationTime = now
        }

        val minutes =
            (BATTLEPASS_RECHARGE_HOURS * 60 - (System.currentTimeMillis() - data.lastGenerationTime) / 1000 / 60).toInt()
        val hours = minutes / 60

        return listOf(
            "Ваши задания на сегодня (Обновление через $hours ${
                Humanize.plurals("час", "часа", "часов", hours)
            } ${minutes % 60} ${
                Humanize.plurals("минута", "минуты", "минут", minutes % 60)
            }):"
        ).plus(data.data!!.mapIndexed { index, quest ->
            " - " + quest.getLore(
                (data.progress?.advanced ?: false) || index < 6
            )
        })
    }
}