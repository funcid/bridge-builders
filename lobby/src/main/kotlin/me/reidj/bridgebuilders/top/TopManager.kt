package me.reidj.bridgebuilders.top

import me.reidj.bridgebuilders.clientSocket
import me.reidj.bridgebuilders.worldMeta
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import me.reidj.bridgebuilders.packages.TopPackage
import me.reidj.bridgebuilders.packages.TopPackage.TopType
import ru.cristalix.boards.bukkitapi.Board
import ru.cristalix.boards.bukkitapi.Boards
import ru.cristalix.boards.bukkitapi.Boards.newBoard
import ru.cristalix.core.GlobalSerializers
import me.reidj.bridgebuilders.tops.TopEntry
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
        worldMeta.getLabels("top").forEach {
            val pair = it.tag.split(" ")
            boards[TopType.valueOf(pair[0].uppercase())] = newBoard("Топ по ${pair[4]}", pair[3], it.add(0.5, 4.5, 0.0), pair[1].toFloat())
        }
    }

    private fun newBoard(title: String, fieldName: String, location: Location, yaw: Float) = newBoard().apply {
        addColumn("#", 20.0)
        addColumn("Игрок", 110.0)
        addColumn(fieldName, 60.0)
        this.title = title
        this.location = Location(worldMeta.world, location.x, location.y, location.z, yaw, 0f)
        Boards.addBoard(this)
    }

    private fun updateData() {
        for (type in TopType.values()) {
            clientSocket.writeAndAwaitResponse<TopPackage>(
                TopPackage(type, DATA_COUNT)
            )
                .thenAcceptAsync { pkg ->
                    tops[type] = pkg.entries.stream()
                        .map { entry ->
                            TopEntry(
                                entry.displayName,
                                TOP_DATA_FORMAT.format(entry.value)
                            )
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