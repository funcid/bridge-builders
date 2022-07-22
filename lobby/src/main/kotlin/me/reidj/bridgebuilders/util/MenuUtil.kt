package me.reidj.bridgebuilders.util

import me.func.mod.data.Sprites
import me.func.mod.selection.button
import me.func.mod.selection.choicer
import me.reidj.bridgebuilders.npc.NpcType
import ru.cristalix.core.realm.IRealmService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object MenuUtil {

    val compass = choicer {
        title = "BridgeBuilders"
        description = "Собери предметы для постройки моста!"
        buttons(
            button {
                texture = Sprites.DUO.path()
                title = "§b4x2"
                description = "Онлайн: §3" + IRealmService.get().getOnlineOnRealms("BRD")
                onClick { it, _, _ -> it.performCommand(NpcType.TWO.command) }
            },
            button {
                texture = Sprites.SQUAD.path()
                title = "§b4x4"
                description = "Онлайн: §3" + IRealmService.get().getOnlineOnRealms("BRI")
                onClick { it, _, _ -> it.performCommand(NpcType.FOUR.command) }
            }
        )
    }
}