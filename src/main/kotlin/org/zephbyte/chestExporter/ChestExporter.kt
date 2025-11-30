package org.zephbyte.chestExporter

import org.bukkit.plugin.java.JavaPlugin

class ChestExporter : JavaPlugin() {
    override fun onEnable() {
        getCommand("exportchest")?.setExecutor(ChestExporterCommand())
        logger.info("ChestExporter has been enabled!")
    }
}
