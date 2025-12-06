package org.zephbyte.chestExporter

import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

/**
 * Main class for the ChestExporter plugin.
 * Initializes the plugin and registers the command executor.
 */
class ChestExporter : JavaPlugin() {
    private lateinit var configManager: ConfigManager

    companion object {
        /**
         * The logger for the plugin, accessible from anywhere.
         */
        lateinit var instance: ChestExporter
            private set
    }

    override fun onEnable() {
        instance = this
        // Initialize the ConfigManager
        configManager = ConfigManager(dataFolder)

        // Register the command executor, passing the ConfigManager
        getCommand("exportchest")?.setExecutor(ChestExporterCommand(configManager))

        logger.info("ChestExporter has been enabled!")
    }
}
