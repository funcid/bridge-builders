package me.reidj.bridgebuilders.ticker.detail

import me.reidj.bridgebuilders.ticker.Ticked
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import org.bukkit.Bukkit

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object DiscordMessage : Ticked {

    private val hoverEvent =
        HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf<BaseComponent>(TextComponent("§eНАЖМИ НА МЕНЯ")))
    private val clickUrl = ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/crGfRk6As4")
    private val alertMessage = ComponentBuilder("\n================\n").color(ChatColor.YELLOW)
        .bold(false)
        .append("§fУ нас есть свой дискорд сервер!")
        .append("§fНе знал?")
        .event(hoverEvent)
        .event(clickUrl)
        .append("§fТогда скорее присоединяйся. §7*Клик*")
        .event(hoverEvent)
        .event(clickUrl)
        .append("\n================\n").color(ChatColor.YELLOW)
        .create()

    override fun tick(vararg args: Int) {
        if (args[0] % 600 == 0) {
            Bukkit.getOnlinePlayers().forEach { it.sendMessage(TextComponent.toLegacyText(*alertMessage)) }
        }
    }
}