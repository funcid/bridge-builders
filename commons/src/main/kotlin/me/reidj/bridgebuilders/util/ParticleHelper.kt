package me.reidj.bridgebuilders.util

import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Location

object ParticleHelper {

    fun happyVillager(location: Location) {
        // Создание частиц возле лука
        worldMeta.world.spawnParticle(
            org.bukkit.Particle.VILLAGER_HAPPY,
            location.clone().add(
                (Math.random() - 0.5) / 0.8,
                (Math.random() + 0.5),
                (Math.random() - 0.5) / 0.8
            ),
            1
        )
    }

}