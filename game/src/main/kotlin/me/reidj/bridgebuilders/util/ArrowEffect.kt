package me.reidj.bridgebuilders.util

import me.reidj.bridgebuilders.App
import me.reidj.bridgebuilders.user.User
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Bukkit
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player

/**
 * @author Рейдж 21.08.2021
 * @project Murder Mystery
 */
class ArrowEffect {

    fun arrowEffect(app: App) {
        Bukkit.getScheduler().runTaskTimer(app, {
            for (entity in worldMeta.world.entities) {
                if (entity is Arrow) {
                    val effect: User = app.getUser(entity.shooter as Player)
                    if (effect.stat.arrowParticle.getParticle() != null)
                        worldMeta.world.spawnParticle(
                            effect.stat.arrowParticle.getParticle(),
                            entity.getLocation(),
                            1
                        )
                }
            }
        }, 1, 1)
    }
}