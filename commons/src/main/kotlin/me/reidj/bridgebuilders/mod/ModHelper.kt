package me.reidj.bridgebuilders.mod

import me.reidj.bridgebuilders.getByPlayer
import me.reidj.bridgebuilders.user.User
import org.bukkit.Bukkit

object ModHelper {

    fun sendTitle(user: me.reidj.bridgebuilders.user.User, text: String) {
        ModTransfer()
            .string(text)
            .send("func:title", user)
    }

    fun notification(user: User?, message: String?) {
        if (user != null) {
            ModTransfer()
                .string(message)
                .send("thepit:notification", user)
        }
    }

    fun allNotification(message: String?) {
        Bukkit.getOnlinePlayers().forEach { player ->
            ModTransfer()
                .string(message)
                .send("thepit:notification", getByPlayer(player))
        }
    }
}