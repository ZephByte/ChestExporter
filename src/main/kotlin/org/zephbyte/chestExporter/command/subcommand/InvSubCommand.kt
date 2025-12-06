package org.zephbyte.chestExporter.command.subcommand

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import org.zephbyte.chestExporter.ChestExporter
import org.zephbyte.chestExporter.command.SubCommand
import org.zephbyte.chestExporter.model.GenerationResult
import org.zephbyte.chestExporter.service.CommandGenerator

/**
 * Handles the /ce inv command.
 */
class InvSubCommand : SubCommand, TabCompleter {
    override val name = "inv"
    override val permission = "chestexporter.inv"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        val targetPlayerName = args.getOrNull(0)
        val targetPlayer: Player?

        if (targetPlayerName == null) {
            if (sender !is Player) {
                sender.sendMessage("${ChatColor.RED}You must specify a player name when running from the console.")
                return
            }
            if (!sender.hasPermission(permission)) {
                sender.sendMessage("${ChatColor.RED}You do not have permission to export your own inventory.")
                return
            }
            targetPlayer = sender
        } else {
            if (!sender.hasPermission("$permission.other")) {
                sender.sendMessage("${ChatColor.RED}You do not have permission to export other players' inventories.")
                return
            }
            targetPlayer = Bukkit.getPlayerExact(targetPlayerName)
            if (targetPlayer == null) {
                sender.sendMessage("${ChatColor.RED}Player '$targetPlayerName' not found.")
                return
            }
        }

        val commandGenerator = CommandGenerator(ChestExporter.instance.configManager)
        sender.sendMessage("${ChatColor.YELLOW}Exporting inventory for ${targetPlayer.name}...")

        val invResult = commandGenerator.generatePlayerInventoryCommand(targetPlayer)
        sendCopyableMessage(sender, invResult.command, "[Click to Copy ${targetPlayer.name}'s Inventory Command]")

        val armorResult = commandGenerator.generatePlayerArmorCommand(targetPlayer)
        if (armorResult != null) {
            sendCopyableMessage(sender, armorResult.command, "[Click to Copy ${targetPlayer.name}'s Armor Command]")
        }

        val combinedIgnored = invResult.ignored + (armorResult?.ignored ?: emptyList())
        val combinedBackported = invResult.backported + (armorResult?.backported ?: emptyList())
        val finalResult = GenerationResult("", combinedIgnored, combinedBackported)

        reportResults(sender as? Player, finalResult)
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (args.size == 1 && sender.hasPermission("$permission.other")) {
            return StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name },
                mutableListOf()
            )
        }
        return emptyList()
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
