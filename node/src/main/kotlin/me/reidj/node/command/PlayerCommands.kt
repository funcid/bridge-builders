package me.reidj.node.command

import me.func.mod.util.command
import me.reidj.bridgebuilders.getLobbyRealm
import me.reidj.node.game.ModifiersManager
import ru.cristalix.core.transfer.ITransferService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class PlayerCommands {

    init {
        command("leave") { player, _ -> ITransferService.get().transfer(player.uniqueId, getLobbyRealm()) }
        command("modifiers") { player, _ -> ModifiersManager.open(player) }
        command("rp") { player, _ -> player.setResourcePack(System.getenv("resourcepack"), "3839afc8-207a-11ed-861d-0242ac120002") }
    }
}