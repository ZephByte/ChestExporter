package org.zephbyte.chestExporter.util

import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta

/**
 * A utility object for converting modern ItemStack metadata into the legacy NBT format.
 */
object NbtConverter {
    /**
     * Converts the metadata of an [ItemStack] into a legacy NBT string.
     */
    @Suppress("DEPRECATION")
    fun convertMetaToLegacyNbt(item: ItemStack): String {
        if (!item.hasItemMeta()) return ""
        val meta = item.itemMeta ?: return ""
        val tagParts = mutableListOf<String>()

        if (meta.hasDisplayName()) {
            val components = TextComponent.fromLegacyText(meta.displayName)
            val jsonName = ComponentSerializer.toString(*components)
            tagParts.add("display:{Name:'${escapeNbtString(jsonName)}'}")
        }

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

        if (meta is EnchantmentStorageMeta) {
            val stored = meta.storedEnchants.entries.joinToString(",") { (enchant, level) ->
                "{id:\"${enchant.key.toString()}\",lvl:${level}s}"
            }
            if (stored.isNotEmpty()) {
                tagParts.add("StoredEnchantments:[$stored]")
                if (item.type != Material.ENCHANTED_BOOK) {
                    tagParts.add("Enchantments:[$stored]")
                }
            }
        } else if (meta.hasEnchants()) {
            val enchants = meta.enchants.entries.joinToString(",") { (enchant, level) ->
                "{id:\"${enchant.key.toString()}\",lvl:${level}s}"
            }
            tagParts.add("Enchantments:[$enchants]")
        }

        if (meta is Damageable && meta.hasDamage()) {
            tagParts.add("Damage:${meta.damage}")
        }

        if (meta.isUnbreakable) {
            tagParts.add("Unbreakable:1b")
        }
        
        if (meta is PotionMeta) {
            meta.basePotionType?.let { basePotionType ->
                val potionTypeStr = basePotionType.name.lowercase()
                val potionName = when {
                    meta.basePotionType?.isUpgradeable == true -> "minecraft:strong_$potionTypeStr"
                    meta.basePotionType?.isExtendable == true -> "minecraft:long_$potionTypeStr"
                    else -> "minecraft:$potionTypeStr"
                }
                tagParts.add("Potion:\"$potionName\"")
            }

            if (meta.hasCustomEffects()) {
                val customEffects = meta.customEffects.joinToString(",") { effect ->
                    val id = effect.type.key.toString()
                    val amplifier = effect.amplifier
                    val duration = effect.duration
                    "{Id:\"$id\",Amplifier:${amplifier}b,Duration:${duration}}"
                }
                if (customEffects.isNotEmpty()) {
                    tagParts.add("CustomPotionEffects:[$customEffects]")
                }
            }
        }

        return tagParts.joinToString(",")
    }

    private fun escapeNbtString(json: String): String {
        return json.replace("'", "\\'")
    }
}
