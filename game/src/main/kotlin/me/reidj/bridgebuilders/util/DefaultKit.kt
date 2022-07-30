package me.reidj.bridgebuilders.util

import me.func.mod.conversation.ModTransfer
import me.reidj.bridgebuilders.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object DefaultKit {

    val armor = arrayOf(
        ItemStack(Material.LEATHER_BOOTS),
        ItemStack(Material.LEATHER_LEGGINGS),
        ItemStack(Material.LEATHER_CHESTPLATE),
        ItemStack(Material.LEATHER_HELMET)
    )
    val sword: ItemStack = ItemStack(Material.WOOD_SWORD)
    val pickaxe = ItemStack(Material.WOOD_PICKAXE)
    val axe = ItemStack(Material.WOOD_AXE)
    val spade = ItemStack(Material.WOOD_SPADE)
    val bread = ItemStack(Material.BREAD, 32)

    fun init(player: Player) {
        val playerTeam = teams.filter { it.players.contains(player.uniqueId) }[0]
        val user = app.getUser(player)!!

        if (slots == 8)
            player.addPotionEffect(fastDigging)

        player.gameMode = org.bukkit.GameMode.SURVIVAL
        player.itemOnCursor = null

        app.teleportAtBase(playerTeam, player)

        player.customName = "${playerTeam.color.chatColor}[${
            playerTeam.color.teamName.substring(
                0,
                1
            )
        }] ${getPrefix(user, true)}"

        teams.forEachIndexed { teamIndex, team ->
            val color = checkColor(team.color)
            Bukkit.getOnlinePlayers().forEach {
                // Отправка прогресса команд
                ModTransfer(
                    teamIndex + 2,
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue()
                ).send("bridge:progressinit", it)
            }
            Bukkit.getOnlinePlayers().forEach { online ->
                ModTransfer(
                    teamIndex + 2,
                    map.needBlocks,
                    team.collected.map { block -> block.value }.sum()
                ).send("bridge:progressupdate", online)
            }
        }
        // Отправка таба
        playerTeam.collected.entries.forEachIndexed { index, block ->
            // Заполнение таба
            ModTransfer(
                index + 2,
                block.key.needTotal,
                block.value,
                block.key.title,
                block.key.getItem()
            ).send("bridge:init", player)
            // Отправка прогресса
            ModTransfer(
                index + 2,
                block.key.needTotal,
                block.value,
                map.needBlocks,
                playerTeam.collected.map { it.value }.sum()
            ).send("bridge:tabupdate", player)
        }
    }
}