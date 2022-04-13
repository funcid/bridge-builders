package me.reidj.bridgebuilders.util

import ru.cristalix.core.network.packages.GroupData
import ru.cristalix.core.permissions.IGroup
import ru.cristalix.core.permissions.IPermissionService

object UtilCristalix {

    fun createDisplayName(data: GroupData): String {
        val prefix: String
        val staffGroup = IPermissionService.get().getGroup(data.playerGroup)
        val donateGroup = IPermissionService.get().getGroup(data.donateGroup)
        prefix = if (staffGroup.prefix.isEmpty()) prefix(donateGroup) else prefix(staffGroup)
        val color =
            if (data.color != null) data.color else if ("PLAYER" != staffGroup.name) staffGroup.nameColor else donateGroup.nameColor
        return (if (prefix.isEmpty()) "" else "$prefix ") + color + data.username
    }

    private fun prefix(group: IGroup): String = if (group.prefix.isEmpty()) "" else group.prefixColor + group.prefix

}