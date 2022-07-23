package me.reidj.bridgebuilders.npc

import clepto.bukkit.B
import implario.humanize.Humanize
import me.func.mod.Banners
import me.func.mod.Npc
import me.func.mod.Npc.location
import me.func.mod.Npc.onClick
import me.func.mod.Npc.skin
import me.func.mod.data.NpcSmart
import me.func.protocol.element.Banner
import me.func.protocol.npc.NpcBehaviour
import me.reidj.bridgebuilders.app
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

    val npcs = mutableMapOf<String, Pair<NpcSmart, Banner>>()

    init {
        NpcType.values().forEach { type ->
            npcs[type.name] = Npc.npc {
                onClick {
                    val player = it.player
                    val user = app.getUser(player) ?: return@onClick
                    if (user.isArmLock)
                        return@onClick
                    user.isArmLock = true
                    player.performCommand(type.command)
                    B.postpone(5) { user.isArmLock = false }
                }
                location(worldMeta.getLabel(type.name.lowercase()).clone().add(.5, .0, .5))
                skin(UUID.fromString(type.skin))
                behaviour = NpcBehaviour.STARE_AT_PLAYER
                pitch = type.pitch
                name = type.npcName
            } to type.banner
        }
    }

    override fun tick(vararg args: Int) {
        if (args[0] % 20 != 0)
            return
        Bukkit.getOnlinePlayers().forEach {
            val stat = app.getUser(it)?.stat
            Banners.content(
                it,
                npcs[NpcType.FOUR.name]!!.second,
                "§b§l4х4\n§e${plural(IRealmService.get().getOnlineOnRealms("BRI"))}"
            )
            Banners.content(
                it,
                npcs[NpcType.TWO.name]!!.second,
                "§b§l4х2\n§e${plural(IRealmService.get().getOnlineOnRealms("BRD"))}"
            )
            Banners.content(
                it,
                npcs[NpcType.GUIDE.name]!!.second,
                "§6${NpcType.GUIDE.bannerTitle}\nПобед: §3${stat?.wins}\nУбийств: §3${stat?.kills}\nСыграно: §3${stat?.games}"
            )
        }
    }

    private fun plural(player: Int) = Humanize.plurals("игрок", "игрока", "игроков", player)
}