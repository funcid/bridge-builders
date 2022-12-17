package me.reidj.lobby.ticker.detail

import com.google.common.collect.Maps
import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.protocol.TopPackage
import me.reidj.bridgebuilders.top.TopEntry
import me.reidj.bridgebuilders.worldMeta
import me.reidj.lobby.ticker.Ticked
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
private const val UPDATE_SECONDS = 30

class TopManager : Ticked {

    private val tops = Maps.newConcurrentMap<String, List<TopEntry<String, String>>>()
    private val boards = Maps.newConcurrentMap<String, Board>()

    private val topDataFormat = DecimalFormat("###,###,###")

    init {
        // Создание топов
        worldMeta.getLabels("top").forEach {
            val pair = it.tag.split(" ")
            boards[pair[0]] = newBoard("Топ по ${pair[4]}", pair[3], it.apply {
                x += 0.5
                y += 4.5
                yaw = pair[1].toFloat()
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

    private fun updateData() {
        for (field in boards.keys) {
            val topPackageResponse =
                clientSocket.writeAndAwaitResponse<TopPackage>(TopPackage(field, DATA_COUNT)).get()
            tops[field] = topPackageResponse.entries.map {
                TopEntry(
                    it.displayName ?: error("displayName is null"),
                    topDataFormat.format(it.value)
                )
            }.toList()
        }
    }

    override fun tick(int: Int) {
        if (int % (20 * UPDATE_SECONDS) != 0)
            return
        updateData()
        val data = GlobalSerializers.toJson(tops)
        if ("{}" == data || data == null) return
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
    }
}