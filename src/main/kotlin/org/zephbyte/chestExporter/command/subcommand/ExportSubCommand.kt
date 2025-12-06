package org.zephbyte.chestExporter.command.subcommand

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.command.CommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.DoubleChestInventory
import org.zephbyte.chestExporter.ChestExporter
import org.zephbyte.chestExporter.command.SubCommand
import org.zephbyte.chestExporter.model.GenerationResult
import org.zephbyte.chestExporter.service.CommandGenerator

/**
 * Handles the /ce export command.
 */
class ExportSubCommand : SubCommand {
    override val name = "export"
    override val permission = "chestexporter.use"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}The 'export' subcommand can only be run by a player.")
            return
        }

        val commandGenerator = CommandGenerator(ChestExporter.instance.configManager)
        val targetedBlock = sender.getTargetBlockExact(10)
        val targetedEntity = getTargetEntity(sender, 10.0)

        when {
            targetedBlock?.state is Container -> {
                val containerState = targetedBlock.state as Container
                handleContainerExport(sender, containerState, commandGenerator)
            }
            targetedEntity is ArmorStand -> {
                handleArmorStandExport(sender, targetedEntity, commandGenerator)
            }
            else -> {
                sender.sendMessage("${ChatColor.RED}You must be looking at a container or an armor stand.")
            }
        }
    }

    private fun getTargetEntity(player: Player, maxDistance: Double): Entity? {
        val result = player.world.rayTraceEntities(
            player.eyeLocation,
            player.eyeLocation.direction,
            maxDistance
        ) { entity -> entity is ArmorStand }
        return result?.hitEntity
    }

    private fun handleContainerExport(sender: Player, containerState: Container, commandGenerator: CommandGenerator) {
        val inventory = containerState.inventory
        sender.sendMessage("${ChatColor.YELLOW}Container export successful!")

        if (containerState is Chest && inventory is DoubleChestInventory) {
            sender.sendMessage("${ChatColor.AQUA}Double chest detected. Generating two commands.")

            val leftChest = inventory.leftSide.holder as Chest
            val rightChest = inventory.rightSide.holder as Chest

            val leftResult = commandGenerator.generateContainerCommand(leftChest, inventory.leftSide)
            val rightResult = commandGenerator.generateContainerCommand(rightChest, inventory.rightSide)

            sendCopyableMessage(sender, leftResult.command, "Click to Copy Left Half Command")
            sendCopyableMessage(sender, rightResult.command, "Click to Copy Right Half Command")

            val combinedResult = GenerationResult(
                "",
                leftResult.ignored + rightResult.ignored,
                leftResult.backported + rightResult.backported
            )
            reportResults(sender, combinedResult)

        } else {
            val result = commandGenerator.generateContainerCommand(containerState, inventory)
            sendCopyableMessage(sender, result.command, "[Click to Copy Container Command]")
            reportResults(sender, result)
        }
    }

    private fun handleArmorStandExport(sender: Player, armorStand: ArmorStand, commandGenerator: CommandGenerator) {
        sender.sendMessage("${ChatColor.YELLOW}Armor stand export successful!")
        val result = commandGenerator.generateArmorStandCommand(armorStand)
        sendCopyableMessage(sender, result.command, "[Click to Copy Armor Stand Command]")
        reportResults(sender, result)
    }

    private fun reportResults(sender: Player?, result: GenerationResult) {
        if (sender == null) return

        if (result.backported.isNotEmpty()) {
            val backportedMsg = result.backported.joinToString(",\n") { (id, amount) -> "$id x$amount" }
            sender.sendMessage("${ChatColor.AQUA}Backported: \n$backportedMsg")
        }
        if (result.ignored.isNotEmpty()) {
            val ignoredMsg = result.ignored.joinToString(",\n") { (id, amount) -> "$id x$amount" }
            sender.sendMessage("${ChatColor.RED}Could not move:\n$ignoredMsg")
        }
    }

    private fun sendCopyableMessage(sender: CommandSender, text: String, title: String) {
        if (sender !is Player) {
            sender.sendMessage("Generated Command: $text")
            return
        }
        val message = TextComponent(title)
        message.color = net.md_5.bungee.api.ChatColor.GREEN
        message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)
        message.hoverEvent =
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Click to copy command to clipboard").create())
        sender.spigot().sendMessage(message)
    }
}
