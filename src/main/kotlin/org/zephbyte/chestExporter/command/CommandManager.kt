package org.zephbyte.chestExporter.command

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.util.StringUtil
import org.zephbyte.chestExporter.command.subcommand.ExportSubCommand
import org.zephbyte.chestExporter.command.subcommand.InvSubCommand
import org.zephbyte.chestExporter.command.subcommand.ReloadSubCommand

/**
 * Manages the /chestexporter command and its subcommands.
 */
class CommandManager : CommandExecutor, TabCompleter {
    private val subcommands = listOf(
        ExportSubCommand(),
        ReloadSubCommand(),
        InvSubCommand()
    ).associateBy { it.name }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommandName = if (args.isEmpty()) "export" else args[0].lowercase()
        val subCommand = subcommands[subCommandName]

        if (subCommand == null) {
            sender.sendMessage("${ChatColor.RED}Unknown subcommand. Usage: /${label} <export|reload|inv>")
            return true
        }

        if (!sender.hasPermission(subCommand.permission)) {
            sender.sendMessage("${ChatColor.RED}You do not have permission to use this command.")
            return true
        }

        subCommand.execute(sender, args.sliceArray(1 until args.size))
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (args.size == 1) {
            return StringUtil.copyPartialMatches(
                args[0],
                subcommands.keys.filter { sender.hasPermission(subcommands[it]!!.permission) },
                mutableListOf()
            )
        }

        // Delegate to subcommand's tab completion if it exists
        val subCommand = subcommands[args[0].lowercase()]
        if (subCommand is TabCompleter) {
            return subCommand.onTabComplete(sender, command, alias, args.sliceArray(1 until args.size))
        }

        return emptyList()
    }
}
