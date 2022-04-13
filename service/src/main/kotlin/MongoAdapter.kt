import com.google.gson.GsonBuilder
import com.mongodb.ClientSessionOptions
import com.mongodb.async.client.MongoClient
import com.mongodb.async.client.MongoCollection
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.*
import com.mongodb.client.result.DeleteResult
import com.mongodb.session.ClientSession
import me.reidj.common.data.Unique
import me.reidj.common.top.TopEntry
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

open class MongoAdapter<T : Unique> constructor(client: MongoClient, database: String, collection: String, type: Class<T>) {

    private val UPSERT = UpdateOptions().upsert(true)

    private var data: MongoCollection<Document>? = null
    private var type: Class<T>? = null
    private var session: ClientSession? = null

    private val connected = AtomicBoolean(false)

    private val gson = GsonBuilder().create()

    init {
        data = client.getDatabase(database).getCollection(collection)
        this.type = type
        val future = CompletableFuture<ClientSession>()
        client.startSession(
            ClientSessionOptions.builder().causallyConsistent(true).build()
        ) { s: ClientSession, throwable: Throwable? ->
            if (throwable != null) future.completeExceptionally(
                throwable
            ) else future.complete(s)
        }
        try {
            session = future[10, TimeUnit.SECONDS]
            Thread {
                try {
                    Thread.sleep(2000L)
                    connected.set(true)
                } catch (ignored: Exception) {
                }
            }.start()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun isConnected(): Boolean {
        return connected.get()
    }

    fun find(uuid: UUID): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        session?.let {
            data!!.find(it, Filters.eq("uuid", uuid.toString()))
                .first { result: Document?, _: Throwable? ->
                    try {
                        future.complete(readDocument(result))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }

        return future
    }

    fun findAll(): CompletableFuture<Map<UUID, T>> {
        val future = CompletableFuture<Map<UUID, T>>()
        val documentFindIterable = session?.let { data!!.find(it) }
        val map: MutableMap<UUID, T> = ConcurrentHashMap()
        documentFindIterable?.forEach({ document: Document? ->
            val obj = readDocument(document)
            obj?.let { map[it.getUuid()!!] = it }
        }) { _: Void?, _: Throwable? -> future.complete(map) }
        return future
    }

    private fun readDocument(document: Document?): T? {
        return if (document == null) null else gson.fromJson(document.toJson(), type)
    }

    fun save(unique: Unique) {
        save(listOf(unique))
    }

    fun save(uniques: List<Unique>) {
        val models: MutableList<WriteModel<Document>> = ArrayList()
        for (unique in uniques) {
            val model: WriteModel<Document> = UpdateOneModel(
                Filters.eq("uuid", unique.getUuid().toString()),
                Document("\$set", Document.parse(gson.toJson(unique))),
                UPSERT
            )
            models.add(model)
        }
        if (session != null && !models.isEmpty()) data!!.bulkWrite(
            session!!, models
        ) { result: BulkWriteResult, throwable: Throwable? ->
            handle(
                result,
                throwable
            )
        }
    }

    fun <V> makeRatingByField(fieldName: String, limit: Int): CompletableFuture<List<TopEntry<T, V>>> {
        val operations = listOf(
            Aggregates.project(
                Projections.fields(
                    Projections.include(fieldName),
                    Projections.include("prefix"),
                    Projections.include("uuid"),
                    Projections.exclude("_id")
                )
            ), Aggregates.sort(Sorts.descending(fieldName)),
            Aggregates.limit(limit)
        )
        val entries: MutableList<TopEntry<T, V>> = ArrayList<TopEntry<T, V>>()
        val future: CompletableFuture<List<TopEntry<T, V>>> = CompletableFuture<List<TopEntry<T, V>>>()
        data!!.aggregate(operations).forEach({ document: Document ->
            val key = readDocument(document)
            if (key != null)
                entries.add(TopEntry(key, document[fieldName] as V))
        }) { _: Void, throwable: Throwable? ->
            if (throwable != null) {
                future.completeExceptionally(throwable)
                return@forEach
            }
            future.complete(entries)
        }
        return future
    }

    private fun handle(result: Any, throwable: Throwable?) {
        throwable?.printStackTrace()
    }

    fun clear(uuid: UUID) {
        data!!.deleteOne(
            Filters.eq("uuid", uuid.toString())
        ) { result: DeleteResult, throwable: Throwable? ->
            handle(
                result,
                throwable
            )
        }
    }
}