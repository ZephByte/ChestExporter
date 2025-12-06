package org.zephbyte.chestExporter

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.util.StringUtil

/**
 * Handles the /chestexporter command and its subcommands.
 *
 * @param configManager The configuration manager for the plugin.
 */
class ChestExporterCommand(private val configManager: ConfigManager) : CommandExecutor, TabCompleter {

    private val subcommands = listOf("export", "reload", "inv")

    /**
     * Executes the /chestexporter command.
     */
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommand = if (args.isEmpty()) "export" else args[0]

        when (subCommand.lowercase()) {
            "export" -> handleExport(sender)
            "reload" -> handleReload(sender)
            "inv" -> handleInventoryExport(sender, args.getOrNull(1))
            else -> sender.sendMessage("${ChatColor.RED}Unknown subcommand. Usage: /${label} <export|reload|inv>")
        }
        return true
    }

    /**
     * Handles tab completion for the /chestexporter command.
     */
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return when (args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], subcommands, mutableListOf())
            2 -> {
                if (args[0].equals("inv", ignoreCase = true) && sender.hasPermission("chestexporter.inv.other")) {
                    StringUtil.copyPartialMatches(
                        args[1],
                        Bukkit.getOnlinePlayers().map { it.name },
                        mutableListOf()
                    )
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
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

    private fun handleExport(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}The 'export' subcommand can only be run by a player.")
            return
        }

        if (!sender.hasPermission("chestexporter.use")) {
            sender.sendMessage("${ChatColor.RED}You do not have permission to use this command.")
            return
        }

        val commandGenerator = CommandGenerator(configManager)
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

    private fun handleInventoryExport(sender: CommandSender, targetPlayerName: String?) {
        val targetPlayer: Player?

        if (targetPlayerName == null) {
            if (sender !is Player) {
                sender.sendMessage("${ChatColor.RED}You must specify a player name when running from the console.")
                return
            }
            if (!sender.hasPermission("chestexporter.inv")) {
                sender.sendMessage("${ChatColor.RED}You do not have permission to export your own inventory.")
                return
            }
            targetPlayer = sender
        } else {
            if (!sender.hasPermission("chestexporter.inv.other")) {
                sender.sendMessage("${ChatColor.RED}You do not have permission to export other players' inventories.")
                return
            }
            targetPlayer = Bukkit.getPlayerExact(targetPlayerName)
            if (targetPlayer == null) {
                sender.sendMessage("${ChatColor.RED}Player '$targetPlayerName' not found.")
                return
            }
        }

        val commandGenerator = CommandGenerator(configManager)
        sender.sendMessage("${ChatColor.YELLOW}Exporting inventory for ${targetPlayer.name}...")

        // Generate inventory command
        val invResult = commandGenerator.generatePlayerInventoryCommand(targetPlayer)
        sendCopyableMessage(sender, invResult.command, "[Click to Copy ${targetPlayer.name}'s Inventory Command]")

        // Generate armor command
        val armorResult = commandGenerator.generatePlayerArmorCommand(targetPlayer)
        if (armorResult != null) {
            sendCopyableMessage(sender, armorResult.command, "[Click to Copy ${targetPlayer.name}'s Armor Command]")
        }

        // Combine and report results
        val combinedIgnored = invResult.ignored + (armorResult?.ignored ?: emptyList())
        val combinedBackported = invResult.backported + (armorResult?.backported ?: emptyList())
        val finalResult = CommandGenerator.GenerationResult("", combinedIgnored, combinedBackported)

        reportResults(sender as? Player, finalResult)
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

            val combinedResult = CommandGenerator.GenerationResult(
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

    private fun handleReload(sender: CommandSender) {
        if (!sender.hasPermission("chestexporter.reload")) {
            sender.sendMessage("${ChatColor.RED}You do not have permission to use this command.")
            return
        }
        configManager.reload()
        sender.sendMessage("${ChatColor.GREEN}ChestExporter configuration reloaded.")
    }

    private fun reportResults(sender: Player?, result: CommandGenerator.GenerationResult) {
        if (sender == null) return // Cannot send messages to console sender in this context

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
