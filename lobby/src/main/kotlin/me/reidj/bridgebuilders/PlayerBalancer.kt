package me.reidj.bridgebuilders

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import ru.cristalix.core.party.IPartyService
import ru.cristalix.core.party.PartySnapshot
import ru.cristalix.core.realm.IRealmService
import ru.cristalix.core.realm.RealmId
import ru.cristalix.core.realm.RealmInfo
import ru.cristalix.core.realm.RealmStatus
import ru.cristalix.core.transfer.ITransferService
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.function.Consumer

class PlayerBalancer : Consumer<Player> {

    private fun getRealm(minToJoin: Int): Optional<RealmId> {
        var maxRealm: RealmInfo? = null
        var minRealm: RealmInfo? = null
        for (realmInfo in IRealmService.get().getRealmsOfType("BRI")) {
            if (realmInfo.status == RealmStatus.GAME_STARTED_RESTRICTED || realmInfo.status == RealmStatus.GAME_STARTED_CAN_SPACTATE
                || realmInfo.currentPlayers + minToJoin > realmInfo.maxPlayers) {
                continue
            }
            if (realmInfo.currentPlayers + minToJoin <= 16) {
                if (maxRealm == null) {
                    maxRealm = realmInfo
                } else if (maxRealm.currentPlayers <= realmInfo.currentPlayers) {
                    maxRealm = realmInfo
                }
            } else {
                if (minRealm == null) {
                    minRealm = realmInfo
                } else if (minRealm.currentPlayers >= realmInfo.currentPlayers) {
                    minRealm = realmInfo
                }
            }
        }
        return if (maxRealm != null) Optional.of(maxRealm.realmId) else Optional.empty()
    }

    override fun accept(player: Player) {
        try {
            val party: Optional<*> = IPartyService.get().getPartyByMember(player.uniqueId).get()
            if (party.isPresent) {
                val party1 = party.get() as PartySnapshot
                if (party1.leader == player.uniqueId) {
                    val realm = getRealm(party1.members.size)
                    if (realm.isPresent) {
                        val realmInfo = IRealmService.get().getRealmById(realm.get())
                        if (realmInfo.currentPlayers + party1.members.size <= realmInfo.maxPlayers
                        ) {
                            for (uuid in party1.members) {
                                ITransferService.get().transfer(uuid, realm.get())
                            }
                        } else {
                            player.spigot().sendMessage(
                                ChatMessageType.ACTION_BAR,
                                TextComponent("§3Не найдено свободных серверов")
                            )
                        }
                    } else {
                        player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            TextComponent("§3Не найдено свободных серверов")
                        )
                    }
                } else {
                    player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent("§3Вы должны быть лидером пати")
                    )
                }
            } else {
                val realmId = getRealm(1)
                if (realmId.isPresent) {
                    ITransferService.get().transfer(player.uniqueId, realmId.get())
                } else {
                    player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent("§3Не найдено свободных серверов")
                    )
                }
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}