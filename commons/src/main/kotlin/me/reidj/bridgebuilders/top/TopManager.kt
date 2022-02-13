package me.reidj.bridgebuilders.top

import me.reidj.bridgebuilders.worldMeta

class TopManager {

    init {
        // Создание топа
        val topLabel = worldMeta.getLabel("top")
        val topArgs = topLabel.tag.split(" ")
        topLabel.setYaw(topArgs[1].toFloat())
        topLabel.setPitch(topArgs[2].toFloat())
        topLabel.add(0.5, 4.5, 0.0)
        TopCreator.create(topLabel, topArgs[3], "Топ по " + topArgs[4], topArgs[0]) { it.wins.toString() }
    }
}