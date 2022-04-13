import com.mongodb.async.client.MongoClient
import me.reidj.common.`package`.TopPackage
import me.reidj.common.top.PlayerTopEntry
import me.reidj.common.top.TopEntry
import me.reidj.common.user.Stat
import me.reidj.common.util.UtilCristalix
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.BulkGroupsPackage
import ru.cristalix.core.network.packages.GroupData
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

class UserDataMongoAdapter(client: MongoClient, dbName: String) : MongoAdapter<Stat>(client, dbName, "data", Stat::class.java) {

    fun getTop(topType: TopPackage.TopType, limit: Int): CompletableFuture<List<PlayerTopEntry<Any>>> {
        return makeRatingByField<TopEntry<Stat, Any>>(topType.name.toLowerCase(), limit).thenApplyAsync { entries ->
            val playerEntries: MutableList<PlayerTopEntry<Any>> =
                ArrayList<PlayerTopEntry<Any>>()
            for (userInfoObjectTopEntry in entries) {
                val objectPlayerTopEntry: PlayerTopEntry<Any> =
                    PlayerTopEntry(userInfoObjectTopEntry.key, userInfoObjectTopEntry.value)
                playerEntries.add(objectPlayerTopEntry)
            }
            try {
                val uuids: MutableList<UUID> = ArrayList()
                for (entry in entries) {
                    val uuid: UUID = entry.key.getUuid()
                    uuids.add(uuid)
                }
                val groups =
                    ISocketClient.get()
                        .writeAndAwaitResponse<BulkGroupsPackage>(
                            BulkGroupsPackage(
                                uuids
                            )
                        )[5L, TimeUnit.SECONDS]
                        .groups
                val map =
                    groups.stream()
                        .collect(
                            Collectors.toMap(
                                { obj: GroupData -> obj.uuid },
                                Function.identity()
                            )
                        )
                for (playerEntry in playerEntries) {
                    val data = map[playerEntry.key.getUuid()]
                    playerEntry.userName = data!!.username
                    playerEntry.displayName = UtilCristalix.createDisplayName(data)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                // Oh shit
                playerEntries.forEach(Consumer { entry: PlayerTopEntry<Any> ->
                    entry.userName = "ERROR"
                    entry.displayName = "ERROR"
                })
            }
            playerEntries
        }
    }

}