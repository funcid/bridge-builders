package me.reidj.bridgebuilders.ticker.detail

import me.reidj.bridgebuilders.ticker.Ticked
import net.md_5.bungee.api.chat.*
import org.bukkit.Bukkit

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object DiscordMessage : Ticked {

    private val hoverEvent =
        HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf<BaseComponent>(TextComponent("§e§lПЕРЕЙТИ")))
    private val clickUrl = ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/crGfRk6As4")
    private val alertMessage = ComponentBuilder("§6§lНовости§f, §9§lобновления§f, §a§lинсайды и многое другое!")
        .bold(false)
        .append("§5§nЗаходи§f, чтобы знать больше, чем другие.")
        .event(hoverEvent)
        .event(clickUrl)
        .create()

    override fun tick(vararg args: Int) {
        if (args[0] % 6000 == 0) {
            Bukkit.getOnlinePlayers().forEach {player -> alertMessage.forEach { player.sendMessage(it) } }
        }
    }
}