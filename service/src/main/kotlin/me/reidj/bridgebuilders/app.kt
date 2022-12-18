package me.reidj.bridgebuilders

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates.unset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.reidj.bridgebuilders.protocol.*
import org.bson.conversions.Bson
import ru.cristalix.core.CoreApi
import ru.cristalix.core.microservice.MicroServicePlatform
import ru.cristalix.core.microservice.MicroserviceBootstrap
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.permissions.IPermissionService
import ru.cristalix.core.permissions.PermissionService

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

fun main() {
    MicroserviceBootstrap.bootstrap(MicroServicePlatform(4))

    val mongoAdapter = MongoAdapter(System.getenv("db_url"), System.getenv("db_data"), "data43")

    ISocketClient.get().run {
        capabilities(
            LoadStatPackage::class,
            SaveUserPackage::class,
            BulkSaveUserPackage::class,
            TopPackage::class,
            RejoinPackage::class
        )

        CoreApi.get().registerService(IPermissionService::class.java, PermissionService(this))

        addListener(LoadStatPackage::class.java) { realmId, pckg ->
            mongoAdapter.find(pckg.uuid).get().run {
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
            val top = mongoAdapter.getTop(pckg.topType, pckg.limit)
            pckg.entries = top
            forward(realmId, pckg)
            println("Top generated for ${realmId.realmName}")
        }
        addListener(RejoinPackage::class.java) { _, pckg ->
            mongoAdapter.find(pckg.uuid).get()?.let {
                it.lastRealm = ""
                mongoAdapter.save(it)
            }
            write(pckg)
        }
    }

    runBlocking {
        val command = readLine()
        if (command == "vipe") {
            val map = mapOf<Bson, Bson>(
                Filters.gt("ether", 0) to unset("ether"),
                Filters.gt("exp", 0.0) to unset("exp"),
                Filters.gt("achievements", listOf<String>()) to unset("achievements"),
                Filters.gt("games", 0) to unset("games"),
                Filters.gt("kills", 0) to unset("kills"),
                Filters.gt("lootBoxes", listOf<String>()) to unset("lootBoxes"),
                Filters.gt("lootBoxOpened", 0) to unset("lootBoxOpened"),
                Filters.gt("wins", 0) to unset("wins"),
                Filters.gt("lastRealm", "") to unset("lastRealm"),
            )
            map.forEach { (key, value) ->
                mongoAdapter.data.updateMany(
                    key,
                    value
                ) { _, throwable -> throwable.printStackTrace() }
            }
        } else if (command == "uban") {
            val map = mapOf<Bson, Bson>(
                Filters.gt("lastRealm", "") to unset("lastRealm")
            )
            map.forEach { (key, value) ->
                mongoAdapter.data.updateMany(
                    key,
                    value
                ) { _, throwable -> throwable?.printStackTrace() }
            }
        }
    }
}