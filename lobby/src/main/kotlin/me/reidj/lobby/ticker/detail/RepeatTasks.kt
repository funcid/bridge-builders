package me.reidj.lobby.ticker.detail

import me.reidj.lobby.ticker.Ticked
import me.reidj.lobby.util.GameUtil
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.*
import org.bukkit.Bukkit
import ru.cristalix.core.realm.IRealmService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class RepeatTasks : Ticked {

    private val hoverEvent =
        HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf<BaseComponent>(TextComponent("§e§lПЕРЕЙТИ")))
    private val clickUrl = ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/crGfRk6As4")
    private val alertMessage = ComponentBuilder("§6§lНовости§f, §9§lобновления§f, §a§lинсайды и многое другое!")
        .bold(false)
        .append("\n")
        .append("§5§nЗаходи§f, чтобы знать больше, чем другие.")
        .event(hoverEvent)
        .event(clickUrl)
        .create()

    override fun tick(args: Int) {
        if (args % 20000 == 0)
            Bukkit.getOnlinePlayers().forEach { player -> player.sendMessage(ChatMessageType.CHAT, *alertMessage) }
        if (args % 20 == 0)
            GameUtil.compass.storage[0].description = "Онлайн: §3" + IRealmService.get().getOnlineOnRealms("BRD")
    }
}