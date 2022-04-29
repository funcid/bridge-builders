package me.reidj.bridgebuilders.top

import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import packages.TopPackage
import packages.TopPackage.TopType
import ru.cristalix.boards.bukkitapi.Board
import ru.cristalix.boards.bukkitapi.Boards
import ru.cristalix.boards.bukkitapi.Boards.newBoard
import ru.cristalix.core.GlobalSerializers
import tops.TopEntry
import java.text.DecimalFormat
import java.util.*
import java.util.stream.Collectors

private const val UPDATE_SECONDS = 30
private const val DATA_COUNT = 10
private val TOP_DATA_FORMAT = DecimalFormat("###,###,###")

class TopManager : Listener, BukkitRunnable() {

    private val tops: MutableMap<TopType, List<TopEntry<String, String>>> = mutableMapOf()
    private val boards: MutableMap<TopType, Board> = mutableMapOf()

    init {
        // Создание топа
        val topLabel = worldMeta.getLabel("top")
        val topArgs = topLabel.tag.split(" ")
        boards[TopType.WINS] = newBoard("Топ по победам", "Побед", topLabel.add(0.5, 4.5, 0.0), topArgs[1].toFloat())
    }

    private fun newBoard(
        title: String, fieldName: String,
        location: Location, yaw: Float
    ): Board {
        val board = newBoard()
        board.addColumn("#", 20.0)
        board.addColumn("Игрок", 110.0)
        board.addColumn(fieldName, 60.0)
        board.title = title
        board.location = Location(worldMeta.world, location.x, location.y, location.z, yaw, 0f)
        Boards.addBoard(board)
        return board
    }


    private fun updateData() {
        for (type in TopType.values()) {
            clientSocket.writeAndAwaitResponse(TopPackage(type, DATA_COUNT))
                .thenAcceptAsync { pkg ->
                    tops[type] = pkg.entries.stream()
                        .map { entry ->
                            TopEntry(entry.displayName, TOP_DATA_FORMAT.format(entry.value))
                        }.collect(Collectors.toList())
                }
        }
    }

    var timer = 0

    override fun run() {
        timer++
        if (timer % (20 * UPDATE_SECONDS) == 0) {
            updateData()
            val data = GlobalSerializers.toJson(tops)
            if ("{}" == data || data == null) return
            boards.forEach { (type: TopType, top: Board) ->
                top.clearContent()
                var counter = 0
                if (tops[type] == null) return@forEach
                for (topEntry in tops[type]!!) {
                    counter++
                    top.addContent(
                        UUID.randomUUID(),
                        "" + counter,
                        topEntry.key,
                        topEntry.value
                    )
                }
                top.updateContent()
            }
        }
    }
}