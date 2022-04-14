package me.reidj.bridgebuilders.top

import org.bukkit.Location
import ru.cristalix.boards.bukkitapi.Boards
import user.Stat

object TopCreator {

    fun create(location: Location, column: String, title: String, key: String, function: (Stat) -> String) {
        val blocks = Boards.newBoard()
        blocks.addColumn("#", 18.0)
        blocks.addColumn("Игрок", 120.0)
        blocks.addColumn(column, 40.0)
        blocks.title = title
        blocks.location = location
        Boards.addBoard(blocks)

        /*Bukkit.getScheduler().scheduleSyncRepeatingTask(
            bridgeBuildersInstance, {
                kensuke.getLeaderboard(userManager, statScope, key, 10).thenAccept {
                    blocks.clearContent()

                    for (entry in it) {
                        if (entry.data.stat.lastSeenName == null)
                            entry.data.stat.lastSeenName =
                                IAccountService.get().getNameByUuid(UUID.fromString(entry.data.session.userId)).get()
                        blocks.addContent(
                            UUID.fromString(entry.data.session.userId),
                            "" + entry.position,
                            entry.data.stat.lastSeenName,
                            "§d" + function(entry.data.stat)
                        )
                    }

                    blocks.updateContent()
                }
            }, 20, 10 * 20
        )*/
    }
}