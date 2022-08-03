package me.reidj.bridgebuilders.ticker.detail

import me.reidj.bridgebuilders.compass
import me.reidj.bridgebuilders.ticker.Ticked
import ru.cristalix.core.realm.IRealmService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
object CompassUpdateOnline : Ticked{

    override fun tick(vararg args: Int) {
        if (args[0] % 20 != 0)
            return
        compass.storage[0].description = "Онлайн: §3" + IRealmService.get().getOnlineOnRealms("BRD")
        compass.storage[1].description = "Онлайн: §3" + IRealmService.get().getOnlineOnRealms("BRI")
    }
}