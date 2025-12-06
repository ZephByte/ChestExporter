package org.zephbyte.chestExporter.command.subcommand

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.zephbyte.chestExporter.ChestExporter
import org.zephbyte.chestExporter.command.SubCommand

/**
 * Handles the /ce reload command.
 */
class ReloadSubCommand : SubCommand {
    override val name = "reload"
    override val permission = "chestexporter.reload"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        ChestExporter.instance.configManager.reload()
        sender.sendMessage("${ChatColor.GREEN}ChestExporter configuration reloaded.")
    }
}
