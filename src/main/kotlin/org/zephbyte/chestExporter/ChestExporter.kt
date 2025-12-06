package org.zephbyte.chestExporter

import org.bukkit.plugin.java.JavaPlugin
import org.zephbyte.chestExporter.command.CommandManager

/**
 * Main class for the ChestExporter plugin.
 */
class ChestExporter : JavaPlugin() {
    lateinit var configManager: ConfigManager
        private set

    companion object {
        lateinit var instance: ChestExporter
            private set
    }

    override fun onEnable() {
        instance = this
        configManager = ConfigManager(dataFolder)

        val commandManager = CommandManager()
        getCommand("chestexporter")?.setExecutor(commandManager)
        getCommand("chestexporter")?.setTabCompleter(commandManager)

        logger.info("ChestExporter has been enabled!")
    }
}
