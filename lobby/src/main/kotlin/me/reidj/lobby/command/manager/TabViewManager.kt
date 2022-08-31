package me.reidj.lobby.command.manager

import me.reidj.bridgebuilders.createPrefix
import me.reidj.bridgebuilders.getUser
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.Listener
import ru.cristalix.core.tab.ITabService
import ru.cristalix.core.tab.TabTextComponent
import ru.cristalix.core.text.TextFormat
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
class TabViewManager : Listener {

    init {
        val tabView = ITabService.get().createConstantTabView()

        tabView.addPrefix(TabTextComponent(1, TextFormat.NONE, { _: UUID -> true }) { uuid: UUID ->
            CompletableFuture.completedFuture(TextComponent.fromLegacyText(createPrefix(getUser(uuid))))
        })
        ITabService.get().defaultTabView = tabView
    }
}