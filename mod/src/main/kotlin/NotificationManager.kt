import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.feder.NetUtil.readUtf8
import ru.cristalix.clientapi.mod
import ru.cristalix.clientapi.registerHandler
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.Relative
import ru.cristalix.uiengine.utility.V3
import ru.cristalix.uiengine.utility.rectangle

object NotificationManager {

    private var board = UIEngine.overlayContext + rectangle {
        size = V3(-12.0, 30.0)
        align = Relative.TOP_RIGHT
        origin = Relative.TOP_RIGHT
        offset = V3(-12.0, 15.0)
    }

    init {
        val minecraft = UIEngine.clientApi.minecraft()

        registerHandler<GameLoop> {
            val inGame = minecraft.inGameHasFocus()
            if(board.enabled && !inGame)
                board.enabled = false
            else if (!board.enabled && inGame)
                board.enabled = true
        }

        App::class.mod.registerChannel("bridge:notification") {
            val text = readUtf8(this)
            val notice = Notification(text)

            board + notice.box

            board.children[board.children.size - 1].animate(0.5) {
                offset.x = -this.size.x
            }

            board.children.reversed().forEachIndexed { index, current ->
                if (index == 6) {
                    current.animate(0.5) {
                        this.offset.x = this.size.x
                    }
                    UIEngine.schedule(0.5) {
                        board.removeChild(current)
                    }
                    board.removeChild(current)
                } else if (index != 0) {
                    current.animate(0.4) {
                        offset.y = (this.size.y + 5.0) * index
                    }
                    current.offset.x = -current.size.x
                }
            }

            UIEngine.schedule(10.0) {
                notice.box.animate(0.5) {
                    this.offset.x = this.size.x
                }
                UIEngine.schedule(0.5) {
                    board.removeChild(notice.box)
                }
            }
        }
    }
}