package org.zephbyte.chestExporter

import org.bukkit.Material
import org.bukkit.block.Chest

object CommandGenerator {
    data class GenerationResult(
        val command: String,
        val ignored: List<Pair<String, Int>>,
        val backported: List<Pair<String, Int>>
    )

    fun generateSingleChestCommand(chest: Chest): GenerationResult {
        val block = chest.block
        val blockData = block.blockData as? org.bukkit.block.data.type.Chest
            ?: return GenerationResult("", listOf(Pair("Error: Not a valid chest block", 0)), listOf())

        val facing = blockData.facing.name.lowercase()
        val type = blockData.type.name.lowercase()

        val blockStateStr = "minecraft:chest[facing=$facing,type=$type]"

        val inv = chest.blockInventory
        val ignoredItems = mutableListOf<Pair<String, Int>>()
        val backportedItems = mutableListOf<Pair<String, Int>>()

        val itemsData = (0 until inv.size).mapNotNull { i ->
            val item = inv.getItem(i)
            if (item == null || item.type == Material.AIR) return@mapNotNull null

            val idKey = "minecraft:${item.type.name.lowercase()}"
            var finalId = idKey

            if (BackportMap.values.containsKey(idKey)) {
                val backportId = BackportMap.values[idKey]
                if (backportId != null) {
                    backportedItems.add(Pair(backportId, item.amount))
                    finalId = backportId
                } else {
                    ignoredItems.add(Pair(idKey, item.amount))
                    return@mapNotNull null
                }
            }

            val tag = NbtConverter.convertMetaToLegacyNbt(item)
            val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""
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