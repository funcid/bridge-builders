package me.reidj.bridgebuilders.`package`

import me.reidj.bridgebuilders.user.Stat
import java.util.*

data class StatPackage constructor(val uuid: UUID, var stat: Stat): BridgePackage()

