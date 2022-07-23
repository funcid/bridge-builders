package me.reidj.bridgebuilders.npc

import me.func.mod.Banners
import me.func.mod.Banners.location
import me.func.protocol.element.Banner
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
    var skin: String,
    val pitch: Float,
    var banner: Banner
) {
    FOUR(
        "4x4",
        "",
        "four",
        "bf30a1df-85de-11e8-a6de-1cb72caa35fd",
        90f,
        createBanner(worldMeta.getLabel("four"), 40, 0.62, 60, 0.5, 5.0,-179.0)
    ),
    TWO(
        "4x2",
        "",
        "two",
        "ca87474e-b15c-11e9-80c4-1cb72caa35fd",
        90f,
        createBanner(worldMeta.getLabel("two"), 40, 0.62, 60, 0.5,5.0, -179.0)
    ),
    GUIDE(
        "Профиль BridgeBuilders",
        "§6ПЕРСОНАЛИЗАЦИЯ",
        "menu",
        "ca87474e-b15c-11e9-80c4-1cb72caa35fd",
        45F,
        createBanner(worldMeta.getLabel("guide"), 50, 0.0, 120, -0.3, 5.5,-144.0)
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
    yaw: Double
) = Banners.new {
    this.height = height
    this.weight = weight
    this.opacity = opacity
    location(location.clone().add(x, y, 1.0))
    motionSettings = hashMapOf(
        "yaw" to yaw,
        "pitch" to 0.0
    )
}