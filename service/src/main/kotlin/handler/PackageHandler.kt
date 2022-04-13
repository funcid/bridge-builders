package handler

import io.netty.channel.Channel
import me.reidj.common.`package`.BridgePackage

@FunctionalInterface
interface PackageHandler<T: BridgePackage> {

    fun handle(channel: Channel, serverName: String, bridgePackage: T)
}