package me.reidj.bridgebuilders.`package`

import me.reidj.bridgebuilders.user.Stat
import java.util.*

data class SaveUserPackage(val user: UUID, val stat: Stat): BridgePackage()
