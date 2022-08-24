package me.reidj.lobby.ticker.detail

import com.google.common.collect.Maps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.protocol.TopPackage
import me.reidj.bridgebuilders.top.TopEntry
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Location
import ru.cristalix.boards.bukkitapi.Board
import ru.cristalix.boards.bukkitapi.Boards
import ru.cristalix.core.GlobalSerializers
import java.text.DecimalFormat
import java.util.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

private const val DATA_COUNT = 10
private val TOP_DATA_FORMAT = DecimalFormat("###,###,###")

class TopManager : () -> Unit {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()

    private val tops = Maps.newConcurrentMap<TopPackage.TopType, List<TopEntry<String, String>>>()
    private val boards = Maps.newConcurrentMap<TopPackage.TopType, Board>()

    init {
        // Создание топов
        worldMeta.getLabels("top").forEach {
            val pair = it.tag.split(" ")
            boards[TopPackage.TopType.valueOf(pair[0].uppercase())] = newBoard("Топ по ${pair[4]}", pair[3], it.apply {
                x += 0.5
                y += 4.5
                yaw =  pair[1].toFloat()
                pitch = 0f
            })
        }
    }

    private fun newBoard(title: String, fieldName: String, location: Location) = Boards.newBoard().also {
        it.addColumn("#", 20.0)
        it.addColumn("Игрок", 110.0)
        it.addColumn(fieldName, 60.0)
        it.title = title
        it.location = location
    }.also(Boards::addBoard)

    private suspend fun updateData() {
        for (field in TopPackage.TopType.values()) {
            val topPackageResponse = clientSocket.writeAndAwaitResponse<TopPackage>(
                TopPackage(
                    field,
                    DATA_COUNT
                )
            ).await()
            tops[field] = topPackageResponse.entries.map { TopEntry(if (it.displayName == null) "ERROR" else it.displayName!!, TOP_DATA_FORMAT.format(it.value)) }
        }
    }

    override fun invoke() {
        if (mutex.isLocked) return
        scope.launch {
            mutex.withLock {
                runCatching {
                    updateData()
                    val data = GlobalSerializers.toJson(tops)
                    if ("{}" == data || data == null) return@withLock
                    boards.forEach { (field, top) ->
                        top.clearContent()
                        var counter = 0
                        if (tops[field] == null) return@forEach
                        tops[field]!!.forEach {
                            counter++
                            top.addContent(
                                UUID.randomUUID(),
                                "" + counter,
                                it.key,
                                it.value
                            )
                        }
                        top.updateContent()
                    }
                }.exceptionOrNull()?.printStackTrace()
            }
        }
    }
}