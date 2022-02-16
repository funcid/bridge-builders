package me.reidj.bridgebuilders.util

import me.func.mod.conversation.ModTransfer
import org.bukkit.entity.Player
import ru.cristalix.core.display.messages.RadioMessage

enum class Music (private val src: String) {

    EXPLOSION("https://implario.dev/arcade/music/EXPLOSION.mp3"),
    RARE_ITEM("https://implario.dev/arcade/music/RARE_ITEM.mp3"),
    BONUS("https://implario.dev/arcade/music/BONUS.mp3"),
    BONUS2("https://implario.dev/arcade/music/SECOND_BONUS.mp3"),
    LEVEL_UP("https://implario.dev/arcade/music/LEVEL_UP.mp3"),
    LEVEL_UP2("https://implario.dev/arcade/music/SECOND_LEVEL_UP.mp3"), ;

    fun sound(vararg player: Player) {
        stop(*player)
        sound(src, *player)
    }

    companion object {
        fun sound(source: String, vararg player: Player) {
            val message = RadioMessage.serialize(RadioMessage(true, source))
            player.forEach { ModTransfer().byteArray(*message).send("ilyafx:radio", it) }
        }

        fun stop(vararg player: Player) = sound("null", *player)
    }
}