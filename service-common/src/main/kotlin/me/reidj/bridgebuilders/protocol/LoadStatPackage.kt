package me.reidj.bridgebuilders.protocol

import me.reidj.bridgebuilders.data.Stat
import ru.cristalix.core.network.CorePackage
import java.util.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class LoadStatPackage(val uuid: UUID): CorePackage() {
    var stat: Stat? = null
}