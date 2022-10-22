package me.reidj.lobby.npc

import implario.humanize.Humanize
import me.func.mod.util.after
import me.func.mod.world.Banners
import me.func.mod.world.Npc
import me.func.mod.world.Npc.location
import me.func.mod.world.Npc.onClick
import me.func.protocol.world.npc.NpcBehaviour
import me.reidj.bridgebuilders.getUser
import me.reidj.bridgebuilders.worldMeta
import me.reidj.lobby.ticker.Ticked
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import ru.cristalix.core.realm.IRealmService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class NpcManager : Ticked, Listener {

    init {
        NpcType.values().forEach { type ->
            Npc.npc {
                onClick {
                    val player = it.player
                    val user = getUser(player) ?: return@onClick
                    if (user.isArmLock)
                        return@onClick
                    user.isArmLock = true
                    player.performCommand(type.command)
                    after(5) { user.isArmLock = false }
                }
                location(worldMeta.getLabel(type.name.lowercase()).apply {
                    x += .5
                    z += .5
                })
                behaviour = NpcBehaviour.STARE_AT_PLAYER
                pitch = type.pitch
                name = type.npcName
                skinUrl = type.skinUrl
                skinDigest = type.skinDigest
            }
        }
    }

    override fun tick(args: Int) {
        if (args % 20 != 0)
            return
        Bukkit.getOnlinePlayers().forEach {
            Banners.content(
                it, NpcType.TWO.banner, "§b§l4х2\n§e${IRealmService.get().getOnlineOnRealms("BRD")} ${
                    plural(
                        IRealmService.get().getOnlineOnRealms("BRD")
                    )
                }"
            )
            val user = getUser(it) ?: return@forEach
            val stat = user.stat
            Banners.content(
                it,
                NpcType.GUIDE.banner,
                "§6${NpcType.GUIDE.bannerTitle}\nПобед: §3${stat.wins}\nУбийств: §3${stat.kills}\nСыграно: §3${stat.games}"
            )
        }
    }

    private fun plural(player: Int) = Humanize.plurals("игрок", "игрока", "игроков", player)
}