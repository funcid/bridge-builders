package me.reidj.common.`package`

import me.reidj.common.user.Stat
import java.util.*

data class StatPackage constructor(val uuid: UUID, var stat: Stat): BridgePackage()

