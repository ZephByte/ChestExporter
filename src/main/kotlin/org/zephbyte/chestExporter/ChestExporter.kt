package org.zephbyte.chestExporter

import org.bukkit.plugin.java.JavaPlugin
import org.zephbyte.chestExporter.command.CommandManager

/**
 * The main entry point for the ChestExporter plugin.
 * This class is responsible for initializing the plugin, setting up the configuration,
 * and registering the command manager.
 */
class ChestExporter : JavaPlugin() {
    /**
     * Manages the plugin's configuration, including the item override map.
     */
    lateinit var configManager: ConfigManager
        private set

    companion object {
        /**
         * A static reference to the plugin instance, providing global access
         * to the logger and configuration.
         */
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
