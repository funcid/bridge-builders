package me.reidj.bridgebuilders.map

import me.reidj.bridgebuilders.data.Block

data class MapData(
    val title: String,
    var blocks: Set<Block>,
    var needBlock: Int
)