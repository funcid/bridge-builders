package me.reidj.lobby.npc

import me.func.mod.world.Banners
import me.func.mod.world.Banners.location
import me.func.protocol.data.element.Banner
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Location

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
enum class NpcType(
    val bannerTitle: String,
    val npcName: String,
    val command: String,
    var skinUrl: String,
    var skinDigest: String,
    val pitch: Float,
    var banner: Banner
) {
    TWO(
        "4x2",
        "",
        "two",
        "https://webdata.c7x.dev/textures/skin/bf30a1df-85de-11e8-a6de-1cb72caa35fd",
        "bf30a1df-85de-11e8-a6de-1cb72caa35fd",
        90f,
        createBanner(worldMeta.getLabel("two"), 40, 0.62, 60, 0.5,5.0, 180.0, false)
    ),
    GUIDE(
        "Профиль BridgeBuilders",
        "§6ПЕРСОНАЛИЗАЦИЯ",
        "menu",
        "self",
        "self",
        45F,
        createBanner(worldMeta.getLabel("guide"), 50, 0.0, 120, -0.3, 5.5,-90.0, true)
    )
    ;
}

private fun createBanner(
    location: Location,
    height: Int,
    opacity: Double,
    weight: Int,
    x: Double,
    y: Double,
    yaw: Double,
    watchPlayer: Boolean,
) = Banners.new {
    this.height = height
    this.weight = weight
    this.opacity = opacity
    location(location.clone().add(x, y, 0.5))
    watchingOnPlayer = watchPlayer
    motionSettings = hashMapOf(
        "yaw" to yaw,
        "pitch" to 0.0
    )
}