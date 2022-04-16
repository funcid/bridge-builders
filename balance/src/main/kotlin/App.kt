import banner.Banners
import dev.xdark.clientapi.event.input.MousePress
import dev.xdark.clientapi.event.lifecycle.GameLoop
import dev.xdark.clientapi.event.network.PluginMessage
import dev.xdark.clientapi.event.render.*
import dev.xdark.clientapi.item.ItemTools
import dev.xdark.clientapi.math.BlockPos
import dev.xdark.clientapi.util.EnumFacing
import dev.xdark.feder.NetUtil
import implario.humanize.Humanize
import lootbox.*
import npc.NpcBehaviour
import npc.NpcData
import npc.NpcManager
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import ru.cristalix.clientapi.KotlinMod
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.eventloop.animate
import ru.cristalix.uiengine.utility.BOTTOM_RIGHT
import ru.cristalix.uiengine.utility.Easings
import ru.cristalix.uiengine.utility.V3
import ru.cristalix.uiengine.utility.text
import java.lang.Math.*
import java.util.*

lateinit var mod: App

class App : KotlinMod() {

    override fun onEnable() {
        UIEngine.initialize(this)

        mod = this

        Banners
        NpcManager

        registerHandler<HealthRender> { isCancelled = true }
        registerHandler<ExpBarRender> { isCancelled = true }
        registerHandler<HungerRender> { isCancelled = true }
        registerHandler<ArmorRender> { isCancelled = true }

        val balanceText = text {
            content = "§aЗагрузка..."
            origin = BOTTOM_RIGHT
            align = BOTTOM_RIGHT
            shadow = true
            offset.y -= 15
            offset.x -= 3
        }

        UIEngine.overlayContext.addChild(balanceText)

        registerChannel("bridge:balance") {
            val money = readInt()
            balanceText.content = "§e${money} ${Humanize.plurals("монета", "монеты", "монет", money)}"
        }

        // Чтение NPC
        registerChannel("npc:spawn") {
            val data = NpcData(
                readInt(),
                UUID.fromString(NetUtil.readUtf8(this)),
                readDouble(),
                readDouble(),
                readDouble(),
                readInt(),
                NetUtil.readUtf8(this),
                NpcBehaviour.values()[readInt()],
                readDouble().toFloat(),
                readDouble().toFloat(),
                NetUtil.readUtf8(this),
                NetUtil.readUtf8(this),
                readBoolean(),
                readBoolean(),
                readBoolean(),
                readBoolean()
            )
            NpcManager.spawn(data)
            NpcManager.show(data.uuid)
        }

        // Скрыть NPC
        registerChannel("npc:hide") {
            NpcManager.hide(UUID.fromString(NetUtil.readUtf8(this)))
        }

        // Показать NPC
        registerChannel("npc:show") {
            NpcManager.show(UUID.fromString(NetUtil.readUtf8(this)))
        }

        // Удалить NPC
        registerChannel("npc:kill") {
            UUID.fromString(NetUtil.readUtf8(this)).apply {
                NpcManager.hide(this)
                NpcManager.kill(this)
            }
        }

        // Обновить метаданные NPC
        registerChannel("npc:update") {
            UUID.fromString(NetUtil.readUtf8(this)).apply {
                NpcManager.get(this)?.let { entity ->
                    entity.entity?.let { npc ->
                        npc.customNameTag = NetUtil.readUtf8(this@registerChannel)

                        npc.teleport(readDouble(), readDouble(), readDouble())
                        npc.setYaw(readDouble().toFloat())
                        npc.setPitch(readDouble().toFloat())

                        if (readBoolean()) npc.enableRidingAnimation()
                        else npc.disableRidingAnimation()
                        if (readBoolean()) npc.enableSleepAnimation(
                            BlockPos.of(
                                npc.x.toInt(),
                                npc.y.toInt(),
                                npc.z.toInt()
                            ), EnumFacing.DOWN
                        )
                        else npc.disableSleepAnimation()
                        npc.isSneaking = readBoolean()
                    }
                }
            }
        }

        registerHandler<GameLoop> {
            val player = clientApi.minecraft().player

            NpcManager.each { _, data ->
                data.entity?.let { entity ->
                    if (data.data.behaviour == NpcBehaviour.NONE)
                        return@let
                    val dx: Double = player.x - entity.x
                    var dy: Double = player.y - entity.y
                    val dz: Double = player.z - entity.z

                    val active = dx * dx + dy * dy + dz * dz < 196

                    dy /= sqrt(dx * dx + dz * dz)
                    val yaw = if (active) (atan2(-dx, dz) / PI * 180).toFloat() else data.data.yaw

                    entity.apply {
                        rotationYawHead = yaw
                        setYaw(yaw)
                        setPitch((atan(-dy) / PI * 180).toFloat())
                    }
                }
            }
        }

        val crateScreen = CrateScreen()
        var ready = false
        var pressed = false

        registerHandler<GameLoop> {
            if (!ready && !crateScreen.opened)
                return@registerHandler

            if (crateScreen.opened && Mouse.isButtonDown(0)) {
                crateScreen.opened = false
                crateScreen.close()
                return@registerHandler
            }

            if (ready && Mouse.isButtonDown(0) && !pressed) {
                pressed = true

                crateScreen.apply {
                    chest.animate(0.5, Easings.QUINT_OUT) {
                        chest.scale = V3(7.0, 7.0, 7.0)
                    }
                }
            }

            if (pressed) {
                clientApi.minecraft().setIngameNotInFocus()
                crateScreen.apply {
                    if (Mouse.isButtonDown(0)) shake()
                    else {
                        pressed = false
                        ready = false
                        open()
                        if (hasNextItem()) {
                            UIEngine.schedule(0.5) {
                                pressed = false
                                ready = true
                            }
                        }
                    }
                }
            }
        }

        registerHandler<MousePress> { isCancelled = ready }

        registerHandler<PluginMessage> {
            if (channel == "lootbox") {
                val amount = data.readInt()

                val loot: MutableList<Loot> = arrayListOf()
                for (i in 0 until amount) {
                    val item = ItemTools.read(data)
                    val name = NetUtil.readUtf8(data)
                    val rarity = when (NetUtil.readUtf8(data)) {
                        "NOTHING" -> NOTHING
                        "COMMON" -> COMMON
                        "UNCOMMON" -> UNCOMMON
                        "RARE" -> RARE
                        "EPIC" -> EPIC
                        "LEGENDARY" -> LEGENDARY
                        "INCREDIBLE" -> INCREDIBLE
                        else -> COMMON
                    }
                    loot.add(Loot(item, name, rarity))
                }

                clientApi.minecraft().setIngameNotInFocus()
                crateScreen.close()
                crateScreen.setup(loot)
                crateScreen.prepareToOpen()
                ready = true
            } else if (channel == "lootbox:close") {
                crateScreen.close()
                ready = false
            }
        }

        registerHandler<RenderTickPre> {
            if (!crateScreen.opened)
                return@registerHandler

            crateScreen.apply {
                val intensity = rotationIntensity.color.alpha

                body1.animate(0.03) {
                    rotation.degrees = (Mouse.getX() / Display.getWidth().toDouble() - 0.5) * Math.PI / 2 * intensity
                }
                body2.animate(0.03) {
                    rotation.degrees = (Mouse.getY() / Display.getHeight().toDouble() - 0.5) * Math.PI / 2 * intensity
                }
                glowRect.animate(0.1) {
                    glowRect.rotation.degrees =
                        -(Mouse.getX() / Display.getWidth().toDouble() - 0.5) * Math.PI / 2 * intensity
                }
            }
        }
    }
}