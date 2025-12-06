package org.zephbyte.chestExporter.command

import org.bukkit.command.CommandSender

/**
 * Represents a subcommand for the /chestexporter command.
 */
interface SubCommand {
    /**
     * The name of the subcommand.
     */
    val name: String

    /**
     * The permission required to execute this subcommand.
     */
    val permission: String

    /**
     * Executes the subcommand.
     *
     * @param sender The sender of the command.
     * @param args The arguments for the subcommand.
     */
    fun execute(sender: CommandSender, args: Array<out String>)
}
