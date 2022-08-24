package me.reidj.lobby.util

import me.func.mod.data.Sprites
import me.func.mod.selection.Reconnect
import me.func.mod.selection.button
import me.func.mod.selection.choicer
import me.func.mod.util.after
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.worldMeta
import me.reidj.lobby.npc.NpcType
import org.bukkit.Location
import ru.cristalix.core.realm.IRealmService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object GameUtil {

    val spawn: Location = worldMeta.getLabel("spawn").apply {
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

    val reconnect = Reconnect(300) {
        val user = getUser(it) ?: return@Reconnect
        if (user.isArmLock)
            return@Reconnect
        user.isArmLock = true
        it.performCommand("rejoin")
        after(5) { user.isArmLock = false }
    }
}