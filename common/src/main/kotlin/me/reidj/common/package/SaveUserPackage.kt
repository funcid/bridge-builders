package me.reidj.common.`package`

import me.reidj.common.user.Stat
import java.util.*

data class SaveUserPackage(val user: UUID, val stat: Stat): BridgePackage()
