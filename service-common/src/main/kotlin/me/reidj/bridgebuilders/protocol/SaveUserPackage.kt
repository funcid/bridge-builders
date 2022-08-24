package me.reidj.bridgebuilders.protocol

import me.reidj.bridgebuilders.data.Stat
import ru.cristalix.core.network.CorePackage
import java.util.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
data class SaveUserPackage(
    val uuid: UUID,
    val stat: Stat
): CorePackage()
