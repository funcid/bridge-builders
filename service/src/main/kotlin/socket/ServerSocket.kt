package socket

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler

open class ServerSocket(private val port: Int) : Thread() {

    private lateinit var channelClass: Class<out ServerSocketChannel?>
    private lateinit var bossGroup: EventLoopGroup
    private lateinit var workerGroup: EventLoopGroup

    init {
        var epoll = true
        try {
            Class.forName("io.netty.channel.epoll.Epoll")
        } catch (e: ClassNotFoundException) {
            epoll = false
        }
        if (epoll) {
            channelClass = EpollServerSocketChannel::class.java
            bossGroup = EpollEventLoopGroup(1)
            workerGroup = EpollEventLoopGroup(1)
        } else {
            channelClass = NioServerSocketChannel::class.java
            bossGroup = NioEventLoopGroup(1)
            workerGroup = NioEventLoopGroup(1)
        }
    }

    override fun run() {
        try {
            val serverBootstrap = ServerBootstrap()
            serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(channelClass)
                .handler(LoggingHandler(LogLevel.INFO))
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(socketChannel: SocketChannel) {
                        socketChannel.pipeline().addLast(
                            HttpRequestDecoder(),
                            HttpObjectAggregator(6553600),
                            HttpResponseEncoder(),
                            WebSocketServerProtocolHandler("/", null, false, 6553600),
                            ServerSocketHandler
                        )
                    }
                })
            serverBootstrap.bind(port).sync().channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}