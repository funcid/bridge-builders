package me.reidj.bridgebuilders.client

import com.google.common.cache.CacheBuilder
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.*
import me.reidj.bridgebuilders.`package`.BridgePackage
import me.reidj.bridgebuilders.`package`.GreetingPackage
import me.reidj.bridgebuilders.util.UtilNetty
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class ClientSocket(val host: String, val port: Int, private val password: String, private val serverName: String) : SimpleChannelInboundHandler<WebSocketFrame>() {

    private var channelClass: Class<out SocketChannel>?
    private var group: EventLoopGroup

    init {
        val epoll: Boolean = try {
            Class.forName("io.netty.channel.epoll.Epoll")
            !java.lang.Boolean.getBoolean("cristalix.net.disable-native-transport") && Epoll.isAvailable()
        } catch (ignored: ClassNotFoundException) {
            false
        }
        if (epoll) {
            channelClass = EpollSocketChannel::class.java
            group = EpollEventLoopGroup(1)
        } else {
            channelClass = NioSocketChannel::class.java
            group = NioEventLoopGroup(1)
        }
    }

    private val responseCache =
        CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build<String, CompletableFuture<*>>()

    private val handlersMap: MutableMap<Class<out BridgePackage>, Consumer<*>> = mutableMapOf()
    private var channel: Channel? = null

    fun connect() {
        Bootstrap()
            .channel(channelClass)
            .group(group)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
            .handler(object : ChannelInitializer<Channel>() {
                override fun initChannel(ch: Channel) {
                    val config = ch.config()
                    config.setOption(ChannelOption.IP_TOS, 24)
                    config.allocator = PooledByteBufAllocator.DEFAULT
                    config.setOption(ChannelOption.TCP_NODELAY, java.lang.Boolean.TRUE)
                    config.setOption(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)
                    ch.pipeline()
                        .addLast(HttpClientCodec())
                        .addLast(HttpObjectAggregator(6553600))
                        .addLast(
                            WebSocketClientProtocolHandler(
                                WebSocketClientHandshakerFactory.newHandshaker(
                                    URI.create("http://$host:$port/"),
                                    WebSocketVersion.V13,
                                    null,
                                    false,
                                    DefaultHttpHeaders(),
                                    6553600
                                ),
                                true
                            )
                        )
                        .addLast(this@ClientSocket)
                }
            })
            .remoteAddress(host, port)
            .connect()
            .addListener(ChannelFutureListener { future: ChannelFuture ->
                if (future.isSuccess) {
                    println("Connection succeeded, bound to: " + future.channel().also { channel = it })
                } else {
                    println("Connection failed")
                    future.cause().printStackTrace()
                    processAutoReconnect()
                }
            })
    }

    private fun sendHandshake() {
        val greetingPackage = GreetingPackage(password, serverName)
        channel!!.writeAndFlush(UtilNetty.toFrame(greetingPackage)).addListeners(
            ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE,
            ChannelFutureListener { future ->
                if (future.isSuccess) {
                    println("Handshake completed!")
                } else {
                    println("Error during handshake")
                    future.cause().printStackTrace()
                    future.channel().close()
                }
            })
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt === WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            sendHandshake()
        }
    }

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: WebSocketFrame) {
        if (msg is TextWebSocketFrame) {
            val pckg: BridgePackage = UtilNetty.readFrame(msg)
            val future = responseCache.getIfPresent(pckg.id)
            if (future != null) {
                responseCache.invalidate(pckg.id)
                future.complete(pckg as Nothing)
            }
            val consumer = handlersMap[pckg::class.java]
            consumer?.accept(pckg as Nothing)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        channel!!.close()
        channel = null
        responseCache.invalidateAll()
        processAutoReconnect()
    }

    fun <T : BridgePackage?> writeAndAwaitResponse(pckg: T): CompletableFuture<T> {
        val future = awaitResponse(pckg)
        write(future as BridgePackage)
        return future
    }

    fun <T : BridgePackage?> awaitResponse(pckg: T): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        responseCache.put(pckg!!.id, future)
        return future
    }

    private fun write(thePitPackage: BridgePackage) {
        UtilNetty.toFrame(thePitPackage)?.let { write(it) }
    }

    private fun write(frame: TextWebSocketFrame) {
        channel!!.writeAndFlush(frame, channel!!.voidPromise())
    }

    fun <T : BridgePackage> registerHandler(clazz: Class<T>, consumer: Consumer<T>) {
        handlersMap[clazz] = consumer
    }

    private fun processAutoReconnect() {
        println("Automatically reconnecting in next 1.5 seconds")
        schedule({ connect() }, 1500L, TimeUnit.MILLISECONDS)
    }

    private fun schedule(command: Runnable, delay: Long, unit: TimeUnit?) {
        group.schedule(command, delay, unit)
    }
}