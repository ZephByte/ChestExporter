package org.zephbyte.chestExporter.command

import org.bukkit.command.CommandSender

/**
 * Defines the contract for all subcommands in the plugin.
 * This interface allows the [CommandManager] to treat all subcommands uniformly,
 * simplifying command dispatch and permission handling.
 */
interface SubCommand {
    /**
     * The unique name of the subcommand (e.g., "export", "reload").
     * This is used to identify the command from the command line.
     */
    val name: String

    /**
     * The permission node required to execute this subcommand.
     */
    val permission: String

    /**
     * The logic to be executed when the subcommand is called.
     *
     * @param sender The entity that executed the command.
     * @param args The arguments provided to the subcommand (excluding the subcommand name itself).
     */
    fun execute(sender: CommandSender, args: Array<out String>)
}
