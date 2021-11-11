package me.reidj.bridgebuilders.user

import java.util.*

data class Stat(
    val id: UUID,

    var wins: Int,
    var games: Int,

    var lastSeenName: String?,
)
