package org.zephbyte.chestExporter

import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.PotionMeta

object NbtConverter {
    @Suppress("DEPRECATION")
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

        // 3. Enchantments (including stored enchants for enchanted books)
        if (meta is EnchantmentStorageMeta) {
            val stored = meta.storedEnchants.entries.joinToString(",") { (enchant, level) ->
                "{id:\"${enchant.key.toString()}\",lvl:${level}s}"
            }
            if (stored.isNotEmpty()) {
                // Keep the StoredEnchantments tag (used by enchanted books)
                tagParts.add("StoredEnchantments:[$stored]")

                // If this item is not actually an ENCHANTED_BOOK, also write the standard
                // Enchantments tag so the stored enchants transfer onto non-book items.
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

        // 4. Damage / Durability
        if (meta is Damageable && meta.hasDamage()) {
            tagParts.add("Damage:${meta.damage}")
        }

        // 5. Unbreakable
        if (meta.isUnbreakable) {
            tagParts.add("Unbreakable:1b")
        }
        
        // 6. Potion Effects
        if (meta is PotionMeta) {
            // Base Potion Type
            meta.basePotionType?.let { basePotionType ->
                val potionTypeStr = basePotionType.name.lowercase()
                val potionName = when {
                    meta.basePotionType?.isUpgradeable == true -> "minecraft:strong_$potionTypeStr"
                    meta.basePotionType?.isExtendable == true -> "minecraft:long_$potionTypeStr"
                    else -> "minecraft:$potionTypeStr"
                }
                tagParts.add("Potion:\"$potionName\"")
            }

            // Custom Effects
            if (meta.hasCustomEffects()) {
                val customEffects = meta.customEffects.joinToString(",") { effect ->
                    val id = effect.type.key.toString()
                    val amplifier = effect.amplifier
                    val duration = effect.duration
                    // NBT tag names are case-sensitive
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