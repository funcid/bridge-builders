package socket

import handlerMap
import password
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketFrame
import io.netty.util.AttributeKey
import me.reidj.common.`package`.BridgePackage
import me.reidj.common.`package`.GreetingPackage
import me.reidj.common.util.UtilNetty
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

object ServerSocketHandler : SimpleChannelInboundHandler<WebSocketFrame>() {

    private val serverInfoKey = AttributeKey.newInstance<String>("serverinfo")

    private val connectedChannels: MutableMap<String, Channel> = ConcurrentHashMap()

    fun broadcast(pckg: BridgePackage) {
        connectedChannels.values.forEach(Consumer { channel: Channel ->
            send(channel, UtilNetty.toFrame(pckg))
        })
    }

    fun send(channel: Channel, pckg: BridgePackage) {
        send(channel, UtilNetty.toFrame(pckg))
    }

    fun send(channel: Channel, frame: TextWebSocketFrame?) {
        channel.writeAndFlush(frame, channel.voidPromise())
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame?) {
        if (msg is TextWebSocketFrame) {
            val thePitPackage: BridgePackage = msg.let { UtilNetty.readFrame(it) }
            val channel = ctx.channel()
            if (thePitPackage is GreetingPackage) {
                if (channel.hasAttr(serverInfoKey)) {
                    println("Some channel tries to authorize, but it already in system!")
                    return
                }
                val pckg: GreetingPackage = thePitPackage
                if (connectedChannels.containsKey(pckg.serverName)) {
                    println("Channel want to register as " + pckg.serverName + ", but this name already in use!")
                    ctx.close()
                    return
                }
                if (pckg.password != password) {
                    println("Channel provided bad password: $password")
                    if (channel.remoteAddress() is InetSocketAddress) {
                        println(channel.remoteAddress().toString())
                    }
                    ctx.close()
                    return
                }
                channel.attr(serverInfoKey).set(pckg.serverName)
                connectedChannels[pckg.serverName] = channel
            } else {
                if (!channel.hasAttr(serverInfoKey)) {
                    println("Some channel tries to send packet without authorization!")
                    if (channel.remoteAddress() is InetSocketAddress) {
                        println(channel.remoteAddress().toString())
                    }
                    ctx.close()
                    return
                }
                val info = channel.attr(serverInfoKey).get()
                Optional.ofNullable(handlerMap[thePitPackage::class.java])
                    .ifPresent { it.handle(channel, info, thePitPackage as Nothing) }
            }
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        if (channel.hasAttr(serverInfoKey)) {
            val name = channel.attr(serverInfoKey).get()
            connectedChannels.remove(name)
            println("Server disconnected! $name")
        }
    }
}