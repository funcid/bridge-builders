
import com.google.common.collect.Maps
import com.mongodb.async.client.MongoClients
import handler.PackageHandler
import io.netty.channel.Channel
import me.reidj.common.`package`.*
import me.reidj.common.user.Stat
import realm.RealmController
import ru.cristalix.core.CoreApi
import ru.cristalix.core.GlobalSerializers
import ru.cristalix.core.microservice.MicroServicePlatform
import ru.cristalix.core.microservice.MicroserviceBootstrap
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.TransferPlayerPackage
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.permissions.PermissionService
import ru.cristalix.core.realm.RealmInfo
import socket.ServerSocket
import socket.ServerSocketHandler.send
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

lateinit var password: String
val handlerMap: MutableMap<Class<out BridgePackage>, PackageHandler<*>> = mutableMapOf()

val metrics: MutableMap<String, BridgeMetricsPackage> = Maps.newConcurrentMap()

lateinit var userData: UserDataMongoAdapter

private lateinit var realmsController: RealmController

fun main() {
    System.setProperty("cristalix.core.net-context-limit", "655360")
    val bridgeServicePort: Int = try {
        System.getenv("BRIDGE_SERVICE_PORT").toInt()
    } catch (exception: NumberFormatException) {
        println("No BRIDGE_SERVICE_PORT environment variable specified!")
        Thread.sleep(1000)
        return
    }
    password = System.getenv("BRIDGE_SERVICE_PASSWORD")
    if (password == null) {
        println("No BRIDGE_SERVICE_PASSWORD environment variable specified!")
        Thread.sleep(1000)
        return
    }
    MicroserviceBootstrap.bootstrap(MicroServicePlatform(2))
    CoreApi.get().registerService(IPermissionService::class.java, PermissionService(ISocketClient.get()))
    val serverSocket = ServerSocket(bridgeServicePort)
    serverSocket.start()
    val dbUrl = System.getenv("db_url")
    val dbName = System.getenv("db_data")
    val client = MongoClients.create(dbUrl)
    userData = UserDataMongoAdapter(client, dbName)
    realmsController = RealmController()

    registerHandler(BridgeMetricsPackage::class.java, object : PackageHandler<BridgeMetricsPackage> {
        override fun handle(channel: Channel, serverName: String, bridgePackage: BridgeMetricsPackage) {
            println("Received metrics.")
            metrics[bridgePackage.serverName] = bridgePackage
        }
    })
    registerHandler(StatPackage::class.java, object : PackageHandler<StatPackage> {
        override fun handle(channel: Channel, serverName: String, bridgePackage: StatPackage) {
            println("Received UserInfoPackage from " + serverName + " for " + bridgePackage.uuid.toString())
            userData.find(bridgePackage.uuid).thenAccept { info ->
                bridgePackage.stat = info
                answer(channel, bridgePackage)
            }
        }
    })
    registerHandler(SaveUserPackage::class.java, object : PackageHandler<SaveUserPackage> {
        override fun handle(channel: Channel, serverName: String, bridgePackage: SaveUserPackage) {
            println("Received SaveUserPackage from " + serverName + " for " + bridgePackage.user.toString())
            userData.save(bridgePackage.stat)
        }
    })
    registerHandler(BulkSaveUserPackage::class.java, object : PackageHandler<BulkSaveUserPackage> {
        override fun handle(channel: Channel, serverName: String, bridgePackage: BulkSaveUserPackage) {
            println("Received BulkSaveUserPackage from $serverName")
            userData.save(bridgePackage.packages.stream().map { it.stat }.collect(Collectors.toList()))
        }
    })
    registerHandler(TopPackage::class.java, object : PackageHandler<TopPackage> {
        override fun handle(channel: Channel, serverName: String, bridgePackage: TopPackage) {
            userData.getTop(bridgePackage.type, bridgePackage.limit).thenAccept { res ->
                bridgePackage.entries = res
                answer(channel, bridgePackage)
            }
        }
    })
    registerHandler(UserRequestJoinPackage::class.java, object : PackageHandler<UserRequestJoinPackage> {
        override fun handle(channel: Channel, serverName: String, bridgePackage: UserRequestJoinPackage) {
            val realm: Optional<RealmInfo> = realmsController.bestRealm()
            var passed = false
            if (realm.isPresent) {
                passed = true
                val realmInfo = realm.get()
                realmInfo.currentPlayers = realmInfo.currentPlayers + 1
                ISocketClient.get().write(
                    TransferPlayerPackage(
                        bridgePackage.user,
                        realmInfo.realmId,
                        emptyMap()
                    )
                )
            }
            bridgePackage.passed = passed
            answer(channel, bridgePackage)
        }
    })
    val consoleThread = Thread { handleConsole() }
    consoleThread.isDaemon = true
    consoleThread.start()
}

private fun handleConsole() {
    val scanner = Scanner(System.`in`)
    scanner.useDelimiter("\n")
    while (true) {
        val s = scanner.next()
        val args = s.split(" ").toTypedArray()
        if (s == "stop") {
            System.exit(0)
            return
        }
        if (s == "players") {
            try {
                val uuidUserInfoMap: Map<UUID, Stat> = userData.findAll().get(5, TimeUnit.SECONDS)
                uuidUserInfoMap.forEach { (k: UUID, v: Stat) ->
                    println(
                        k.toString() + ": " + GlobalSerializers.toJson(v)
                    )
                }
                println(uuidUserInfoMap.size.toString() + " players in total.")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (args[0] == "delete") {
            if (args.size < 2) println("Usage: delete [uuid]") else {
                if (args[1] == "all") {
                    userData.findAll().thenAccept { map ->
                        map.forEach { uuid, userInfo ->
                            userData.clear(uuid)
                            println("Removed $uuid")
                        }
                    }
                } else {
                    val uuid = UUID.fromString(args[1])
                    userData.clear(uuid)
                    println("Removed $uuid's data from db...")
                }
            }
        }
    }
}

private fun createMetrics(): String {
    val newMetrics: MutableMap<String, BridgeMetricsPackage.PacketMetric> = Maps.newHashMap()
    val builder = StringBuilder()
    metrics.forEach { (source, data) ->
        builder.append("online{realm=\"").append(source).append("\"} ").append(data.online).append("\n")
        builder.append("tps{realm=\"").append(source).append("\"} ").append(data.tps).append("\n")
        builder.append("free_memory{realm=\"").append(source).append("\"} ").append(data.freeMemory)
            .append("\n")
        builder.append("allocated_memory{realm=\"").append(source).append("\"} ").append(data.allocatedMemory)
            .append("\n")
        builder.append("total_memory{realm=\"").append(source).append("\"} ").append(data.totalMemory).append("\n")
        data.metrics.forEach { (key, value) ->
            newMetrics.compute(key) { _: String?, old: BridgeMetricsPackage.PacketMetric? ->
                if (old != null) {
                    old.compressedBytes = old.compressedBytes + value.compressedBytes
                    old.decompressedBytes = old.decompressedBytes + value.decompressedBytes
                    old.received = old.received + value.received
                    old.receivedBytes = old.receivedBytes + value.receivedBytes
                    old.sent = old.sent + value.sent
                    old.sentBytes = old.sentBytes + value.sentBytes
                    return@compute old
                }
                value.clone()
            }
        }
    }
    newMetrics.forEach { (key: String, value: BridgeMetricsPackage.PacketMetric) ->
        builder.append("compressed_bytes{packet=\"").append(key).append("\"} ").append(value.compressedBytes)
            .append("\n")
        builder.append("decompressed_bytes{packet=\"").append(key).append("\"} ")
            .append(value.decompressedBytes)
            .append("\n")
        builder.append("received{packet=\"").append(key).append("\"} ").append(value.received).append("\n")
        builder.append("received_bytes{packet=\"").append(key).append("\"} ").append(value.receivedBytes)
            .append("\n")
        builder.append("sent{packet=\"").append(key).append("\"} ").append(value.sent).append("\n")
        builder.append("sent_bytes{packet=\"").append(key).append("\"} ").append(value.sentBytes).append("\n")
    }
    return builder.toString()
}

/**
 * Send package to socket
 *
 * @param pckg package
 */
private fun answer(channel: Channel, pckg: BridgePackage) {
    send(channel, pckg)
}

/**
 * Register handler to package type
 *
 * @param clazz   class of package
 * @param handler handler
 * @param <T>     package type
</T> */
private fun <T : BridgePackage> registerHandler(clazz: Class<T>, handler: PackageHandler<T>) {
    handlerMap[clazz] = handler
}

