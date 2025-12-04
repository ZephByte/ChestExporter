package org.zephbyte.chestExporter

import org.bukkit.Material
import org.bukkit.block.Container

object CommandGenerator {
    data class GenerationResult(
        val command: String,
        val ignored: List<Pair<String, Int>>,
        val backported: List<Pair<String, Int>>
    )

    fun generateContainerCommand(container: Container): GenerationResult {
        val block = container.block
        val blockData = block.blockData
        val blockStateStr = blockData.asString.replaceFirst("minecraft:", "")

        val inv = container.inventory
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