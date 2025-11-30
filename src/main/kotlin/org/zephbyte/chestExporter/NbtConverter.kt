package org.zephbyte.chestExporter

import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

object NbtConverter {
    fun convertMetaToLegacyNbt(item: ItemStack): String {
        if (!item.hasItemMeta()) return ""
        val meta = item.itemMeta ?: return ""
        val tagParts = mutableListOf<String>()

        // 1. Display Name
        if (meta.hasDisplayName()) {
            val components = TextComponent.fromLegacyText(meta.displayName)
            val jsonName = ComponentSerializer.toString(*components)
            tagParts.add("display:{Name:'${escapeNbtString(jsonName)}'}")
        }

        // 2. Lore
        if (meta.hasLore()) {
            val lore = meta.lore
            if (!lore.isNullOrEmpty()) {
                val jsonLines = lore.joinToString(",") { line ->
                    val components = TextComponent.fromLegacyText(line)
                    "'${escapeNbtString(ComponentSerializer.toString(*components))}'"
                }
                val loreString = "Lore:[$jsonLines]"
                val existingDisplay = tagParts.find { it.startsWith("display:") }
                if (existingDisplay != null) {
                    tagParts.remove(existingDisplay)
                    val nameContent = existingDisplay.removePrefix("display:{").removeSuffix("}")
                    tagParts.add("display:{$nameContent,$loreString}")
                } else {
                    tagParts.add("display:{$loreString}")
                }
            }
        }

        // 3. Enchantments
        if (meta.hasEnchants()) {
            val enchants = meta.enchants.entries.joinToString(",") { (enchant, level) ->
                "{id:\"${enchant.key.key}\",lvl:${level}s}"
            }
            tagParts.add("Enchantments:[$enchants]")
        }

        // 4. Damage / Durability
        if (meta is Damageable && meta.hasDamage()) {
            tagParts.add("Damage:${meta.damage}")
        }

        // 5. Unbreakable
        if (meta.isUnbreakable) {
            tagParts.add("Unbreakable:1b")
        }

        return tagParts.joinToString(",")
    }

    private fun escapeNbtString(json: String): String {
        return json.replace("'", "\\'")
    }
}