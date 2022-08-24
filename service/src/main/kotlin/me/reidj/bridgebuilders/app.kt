package me.reidj.bridgebuilders

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates.unset
import kotlinx.coroutines.runBlocking
import me.reidj.bridgebuilders.protocol.*
import org.bson.conversions.Bson
import ru.cristalix.core.CoreApi
import ru.cristalix.core.microservice.MicroServicePlatform
import ru.cristalix.core.microservice.MicroserviceBootstrap
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.permissions.PermissionService
import java.util.concurrent.TimeUnit

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

fun main() {
    MicroserviceBootstrap.bootstrap(MicroServicePlatform(3))

    val mongoAdapter = MongoAdapter(System.getenv("db_url"), System.getenv("db_data"), "data43")

    ISocketClient.get().run {
        capabilities(LoadStatPackage::class, SaveUserPackage::class, BulkSaveUserPackage::class, TopPackage::class, RejoinPackage::class)

        CoreApi.get().registerService(IPermissionService::class.java, PermissionService(this))

        addListener(LoadStatPackage::class.java) { realmId, pckg ->
            mongoAdapter.find(pckg.uuid).get(3, TimeUnit.SECONDS).apply {
                pckg.stat = this
                forward(realmId, pckg)
                println("Loaded on ${realmId.realmName}! Player: ${pckg.uuid}")
            }
        }
        addListener(SaveUserPackage::class.java) { realmId, pckg ->
            mongoAdapter.save(pckg.stat)
            println("Received SaveUserPackage from ${realmId.realmName} for ${pckg.uuid}")

        }
        addListener(BulkSaveUserPackage::class.java) { realmId, pckg ->
            mongoAdapter.save(pckg.packages.map { it.stat })
            println("Received BulkSaveUserPackage from ${realmId.realmName}")
        }
        addListener(TopPackage::class.java) { realmId, pckg ->
            mongoAdapter.getTop(pckg.topType, pckg.limit).thenAccept {
                pckg.entries = it
                forward(realmId, pckg)
                println("Top generated for ${realmId.realmName}")
            }
        }
        addListener(RejoinPackage::class.java) { _, pckg ->
            write(pckg)
        }
    }

    runBlocking {
        val command = readLine()
        if (command == "vipe") {
            val map = mapOf<Bson, Bson>(
                Filters.gt("money", 0) to unset("money"),
                // TODO Тут ебанёт скорее всего надо поменять
                Filters.gt("achievement", listOf<String>()) to unset("achievement"),
                Filters.gt("games", 0) to unset("games"),
                Filters.gt("kills", 0) to unset("kills"),
                Filters.gt("lootBox", 0) to unset("lootBox"),
                Filters.gt("lootBoxOpened", 0) to unset("lootBoxOpened"),
                Filters.gt("isBan", false) to unset("isBan"),
                Filters.gt("wins", 0) to unset("wins"),
                Filters.gt("lastRealm", "") to unset("lastRealm"),
                Filters.gt("gameLockTime", 0) to unset("gameLockTime"),
                Filters.gt("gameExitTime", 0) to unset("gameExitTime")
            )
            map.forEach { (key, value) ->
                mongoAdapter.data.updateMany(
                    key,
                    value
                ) { _, throwable -> throwable.printStackTrace() }
            }
        } else if (command == "unbanall") {
            val map = mapOf<Bson, Bson>(
                Filters.gt("lastRealm", "") to unset("lastRealm"),
                Filters.gt("gameLockTime", 0) to unset("gameLockTime"),
                Filters.gt("gameExitTime", 0) to unset("gameExitTime")
            )
            map.forEach { (key, value) ->
                mongoAdapter.data.updateMany(
                    key,
                    value
                ) { _, throwable -> throwable.printStackTrace() }
            }
        }
    }
}