package me.reidj.bridgebuilders

import com.mongodb.ClientSessionOptions
import com.mongodb.async.client.MongoClient
import com.mongodb.async.client.MongoClients
import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.*
import com.mongodb.session.ClientSession
import me.reidj.bridgebuilders.data.Stat
import me.reidj.bridgebuilders.protocol.TopPackage.TopType
import me.reidj.bridgebuilders.top.PlayerTopEntry
import me.reidj.bridgebuilders.top.TopEntry
import me.reidj.bridgebuilders.util.UtilCristalix
import org.bson.Document
import ru.cristalix.core.GlobalSerializers
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.BulkGroupsPackage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


/**
 * @project : forest
 * @author : Рейдж
 **/
open class MongoAdapter(dbUrl: String, dbName: String, collection: String) {

    var data: MongoCollection<Document>

    private val upsert = UpdateOptions().upsert(true)
    private val mongoClient: MongoClient
    private val session: ClientSession

    init {
        val future = CompletableFuture<ClientSession>()
        mongoClient = MongoClients.create(dbUrl).apply {
            startSession(ClientSessionOptions.builder().causallyConsistent(true).build()) { response, throwable ->
                if (throwable != null) future.completeExceptionally(throwable) else future.complete(response)
            }
        }
        data = mongoClient.getDatabase(dbName).getCollection(collection)
        session = future.get(10, TimeUnit.SECONDS)
    }

    fun find(uuid: UUID) = CompletableFuture<Stat?>().apply {
        data.find(session, Filters.eq("uuid", uuid.toString())).first { result: Document?, _: Throwable? ->
            try {
                complete(readDocument(result))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun findAll(): CompletableFuture<Map<UUID, Stat>> {
        val future = CompletableFuture<Map<UUID, Stat>>()
        val documentFindIterable = data.find()
        val map = ConcurrentHashMap<UUID, Stat>()
        documentFindIterable.forEach({ document: Document ->
            val obj: Stat? = readDocument(document)
            if (obj != null)
                map[obj.uuid] = obj
        }) { _: Void, _: Throwable -> future.complete(map) }
        return future
    }

    private fun readDocument(document: Document?) =
        if (document == null) null else GlobalSerializers.fromJson(document.toJson(), Stat::class.java)

    fun save(stat: Stat) = save(listOf(stat))

    fun save(stats: List<Stat>) {
        mutableListOf<WriteModel<Document>>().apply {
            stats.forEach {
                add(
                    UpdateOneModel(
                        Filters.eq("uuid", it.uuid.toString()),
                        Document("\$set", Document.parse(GlobalSerializers.toJson(it))),
                        upsert
                    )
                )
            }
        }.run {
            if (isNotEmpty())
                data.bulkWrite(session, this) { _, throwable: Throwable? -> handle(throwable) }
        }
    }

    private fun handle(throwable: Throwable?) = throwable?.printStackTrace()

    open fun <V> makeRatingByField(fieldName: String, limit: Int): CompletableFuture<List<TopEntry<Stat, V>>> {
        val entries = ArrayList<TopEntry<Stat, V>>()
        val future: CompletableFuture<List<TopEntry<Stat, V>>> = CompletableFuture<List<TopEntry<Stat, V>>>()

        data.createIndex(Indexes.hashed("_id")) { _, _ -> }
        data.createIndex(Indexes.hashed("uuid")) { _, _ -> }
        data.createIndex(Indexes.ascending(fieldName)) { _, _ -> }

        data.aggregate(listOf(
            Aggregates.project(
                Projections.fields(
                    Projections.include(fieldName),
                    Projections.include("uuid"),
                    Projections.exclude("_id")
                )
            ), Aggregates.sort(Sorts.descending(fieldName)),
            Aggregates.limit(limit)
        )).forEach({ document: Document ->
            readDocument(document)?.let { entries.add(TopEntry(it, document[fieldName] as V)) }
        }) { _: Void?, throwable: Throwable? ->
            if (throwable != null) {
                future.completeExceptionally(throwable)
                return@forEach
            }
            future.complete(entries)
        }
        return future
    }

    open fun getTop(topType: TopType, limit: Int) = CompletableFuture<MutableList<PlayerTopEntry<Any>>>().apply {
        makeRatingByField<String>(topType.name.lowercase(), limit).thenApplyAsync { entries ->
            val playerEntries = mutableListOf<PlayerTopEntry<Any>>()
            entries.forEach { it.key.let { stat -> playerEntries.add(PlayerTopEntry(stat, it.value)) } }
            try {
                val uuids = arrayListOf<UUID>()
                entries.forEach { uuids.add(it.key.uuid) }
                val groups = ISocketClient.get()
                    .writeAndAwaitResponse<BulkGroupsPackage>(BulkGroupsPackage(uuids))
                    .get(5L, TimeUnit.SECONDS)
                    .groups
                val map = groups.associateBy { it.uuid }
                playerEntries.forEach {
                    val data = map[it.key.uuid]
                    it.userName = if (data == null) "ERROR" else data.username
                    it.displayName = if (data == null) "ERROR" else UtilCristalix.createDisplayName(data)
                }
            } catch (exception: java.lang.Exception) {
                exception.printStackTrace()
            }
            complete(playerEntries)
        }
    }

}