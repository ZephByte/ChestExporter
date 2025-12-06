package org.zephbyte.chestExporter

import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.inventory.Inventory

/**
 * Generates a /setblock command to recreate a container with its items in a legacy Minecraft version.
 *
 * @param configManager The configuration manager for the plugin.
 */
class CommandGenerator(private val configManager: ConfigManager) {
    /**
     * Represents the result of a command generation, including the command itself,
     * and lists of items that were ignored or backported.
     */
    data class GenerationResult(
        val command: String,
        val ignored: List<Pair<String, Int>>,
        val backported: List<Pair<String, Int>>
    )

    /**
     * Generates a /setblock command for the given container.
     *
     * @param container The container state to generate the command for.
     * @param inventory The specific inventory to serialize (e.g., one half of a double chest).
     * @return A [GenerationResult] containing the command and lists of ignored and backported items.
     */
    fun generateContainerCommand(container: Container, inventory: Inventory): GenerationResult {
        val block = container.block
        val blockData = container.blockData
        val blockStateStr = blockData.asString.replaceFirst("minecraft:", "")

        val ignoredItems = mutableListOf<Pair<String, Int>>()
        val backportedItems = mutableListOf<Pair<String, Int>>()
        val overrideMap = configManager.getOverrideMap()

        val itemsData = (0 until inventory.size).mapNotNull { i ->
            val item = inventory.getItem(i)
            if (item == null || item.type == Material.AIR) return@mapNotNull null

            val idKey = "minecraft:${item.type.name.lowercase()}"
            var finalId = idKey

            if (overrideMap.containsKey(idKey)) {
                val overrideId = overrideMap[idKey]
                if (overrideId != null && overrideId.isNotBlank()) {
                    backportedItems.add(Pair(overrideId, item.amount))
                    finalId = overrideId
                } else {
                    ignoredItems.add(Pair(idKey, item.amount))
                    return@mapNotNull null
                }
            }

            val tag = NbtConverter.convertMetaToLegacyNbt(item)
            val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""
            // The slot index 'i' is correct here because we are iterating over a single inventory view (size 27).
            "{Slot:${i}b,id:\"$finalId\",Count:${item.amount}b$tagString}"
        }

        val itemsNbt = if (itemsData.isNotEmpty()) {
            itemsData.joinToString(separator = ",", prefix = "{Items:[", postfix = "]}")
        } else {
            ""
        }

        val command = "/setblock ${block.x} ${block.y} ${block.z} $blockStateStr$itemsNbt replace"
        return GenerationResult(command, ignoredItems, backportedItems)
    }
}
