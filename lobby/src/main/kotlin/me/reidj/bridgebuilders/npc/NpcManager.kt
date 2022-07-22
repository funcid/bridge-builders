package me.reidj.bridgebuilders.npc

import me.func.mod.Banners
import me.func.mod.Npc
import me.func.mod.Npc.location
import me.func.mod.Npc.onClick
import me.func.mod.Npc.skin
import me.func.protocol.element.Banner
import me.func.protocol.npc.NpcBehaviour
import me.reidj.bridgebuilders.ticker.Ticked
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Bukkit
import ru.cristalix.core.realm.IRealmService
import java.util.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object NpcManager : Ticked {

    private val banners = mutableMapOf<String, Banner>()

    init {
        NpcType.values().forEach { type ->
            banners[type.name] = type.banner
            Npc.npc {
                onClick { it.player.performCommand(type.command) }
                location(worldMeta.getLabel(type.name.lowercase()).clone().add(.5, .0, .5))
                skin(UUID.fromString(type.skin))
                behaviour = NpcBehaviour.STARE_AT_PLAYER
                pitch = type.pitch
            }
        }
    }

    override fun tick(vararg args: Int) {
        if (args[0] % 20 != 0)
            return
        Bukkit.getOnlinePlayers().forEach {
            Banners.content(it, banners[NpcType.FOUR.name]!!, "§6§l4х4\n§bОнлайн: ${IRealmService.get().getOnlineOnRealms("BRI")}")
            Banners.content(it, banners[NpcType.TWO.name]!!, "§6§l4х2\n§bОнлайн: ${IRealmService.get().getOnlineOnRealms("BRD")}")
        }
    }
}