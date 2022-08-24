package me.reidj.bridgebuilders.protocol

import ru.cristalix.core.network.CorePackage

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
data class BulkSaveUserPackage(val packages: List<SaveUserPackage>): CorePackage()
