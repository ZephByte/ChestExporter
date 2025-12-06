package org.zephbyte.chestExporter

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.DoubleChestInventory

/**
 * Handles the /exportchest command, which allows players to export the contents of a container
 * as a /setblock command for a legacy Minecraft version.
 *
 * @param configManager The configuration manager for the plugin.
 */
class ChestExporterCommand(private val configManager: ConfigManager) : CommandExecutor {

    /**
     * Executes the /exportchest command.
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be run by a player.")
            return true
        }

        if (!sender.hasPermission("chestexporter.use")) {
            sender.sendMessage("${ChatColor.RED}You do not have permission to use this command.")
            return true
        }

        val block = sender.getTargetBlockExact(10)

        if (block == null || block.state !is Container) {
            sender.sendMessage("${ChatColor.RED}You must be looking at a container.")
            return true
        }

        val containerState = block.state as Container
        val inventory = containerState.inventory
        val commandGenerator = CommandGenerator(configManager)

        sender.sendMessage("${ChatColor.YELLOW}Export Successful!")

        if (containerState is Chest && inventory is DoubleChestInventory) {
            sender.sendMessage("${ChatColor.AQUA}Double chest detected. Generating two commands.")

            val leftChest = inventory.leftSide.holder as Chest
            val rightChest = inventory.rightSide.holder as Chest

            // Pass the specific inventory half to the generator
            val leftResult = commandGenerator.generateContainerCommand(leftChest, inventory.leftSide)
            val rightResult = commandGenerator.generateContainerCommand(rightChest, inventory.rightSide)

            sendCopyableMessage(sender, leftResult.command, "Click to Copy Left Half Command")
            sendCopyableMessage(sender, rightResult.command, "Click to Copy Right Half Command")

            val combinedResult = CommandGenerator.GenerationResult(
                "", // Not used
                leftResult.ignored + rightResult.ignored,
                leftResult.backported + rightResult.backported
            )
            reportResults(sender, combinedResult)

        } else {
            // For single containers, the container's inventory is correct
            val result = commandGenerator.generateContainerCommand(containerState, inventory)
            sendCopyableMessage(sender, result.command)
            reportResults(sender, result)
        }

        return true
    }

    /**
     * Reports the results of the command generation to the player, including backported and ignored items.
     *
     * @param sender The player who executed the command.
     * @param result The result of the command generation.
     */
    private fun reportResults(sender: Player, result: CommandGenerator.GenerationResult) {
        if (result.backported.isNotEmpty()) {
            val backportedMsg = result.backported.joinToString(",\n") { (id, amount) -> "$id x$amount" }
            sender.sendMessage("${ChatColor.AQUA}Backported: \n$backportedMsg")
        }
        if (result.ignored.isNotEmpty()) {
            val ignoredMsg = result.ignored.joinToString(",\n") { (id, amount) -> "$id x$amount" }
            sender.sendMessage("${ChatColor.RED}Could not move:\n$ignoredMsg")
        }
    }

    /**
     * Sends a copyable message to the player's chat.
     *
     * @param player The player to send the message to.
     * @param text The text to be copied when the message is clicked.
     * @param title The title of the message.
     */
    private fun sendCopyableMessage(player: Player, text: String, title: String = "[Click to Copy 1.20 Command]") {
        val message = TextComponent(title)
        message.color = net.md_5.bungee.api.ChatColor.GREEN
        message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)
        message.hoverEvent =
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Click to copy command to clipboard").create())
        player.spigot().sendMessage(message)
    }
}
