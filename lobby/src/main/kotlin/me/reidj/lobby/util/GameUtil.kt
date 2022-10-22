package me.reidj.lobby.util

import me.func.mod.conversation.data.Sprites
import me.func.mod.ui.menu.button
import me.func.mod.ui.menu.choicer
import me.reidj.bridgebuilders.worldMeta
import me.reidj.lobby.npc.NpcType
import org.bukkit.Location
import ru.cristalix.core.realm.IRealmService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object GameUtil {

    val spawn: Location = worldMeta.getLabel("spawn").clone().apply {
        x += .5
        z += .5
        yaw = 90f
    }

    val compass = choicer {
        title = "BridgeBuilders"
        description = "Собери предметы для постройки моста!"
        buttons(
            button {
                texture = Sprites.DUO.path()
                title = "§b4x2"
                hint("Играть")
                description = "Онлайн: §3" + IRealmService.get().getOnlineOnRealms("BRD")
                onClick { it, _, _ -> it.performCommand(NpcType.TWO.command) }
            }
        )
    }
}