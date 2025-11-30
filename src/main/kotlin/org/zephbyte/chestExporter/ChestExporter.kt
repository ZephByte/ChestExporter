package org.zephbyte.chestExporter

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.plugin.java.JavaPlugin

class ChestExporter : JavaPlugin(), CommandExecutor {

    override fun onEnable() {
        // Register the command defined in plugin.yml
        getCommand("exportchest")?.setExecutor(this)
        logger.info("ChestExporter has been enabled!")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${ChatColor.RED}This command can only be run by a player.")
            return true
        }

        if (!sender.hasPermission("chestexporter.use")) {
            sender.sendMessage("${ChatColor.RED}You do not have permission to use this command.")
            return true
        }

        // Raytrace to find the block the player is looking at (max distance 10 blocks)
        val block = sender.getTargetBlockExact(10)

        if (block == null || block.state !is Chest) {
            sender.sendMessage("${ChatColor.RED}You must be looking directly at a Chest.")
            return true
        }

        val chest = block.state as Chest
        val setblockCommand = generateLegacyCommand(chest, block)

        // Send the command to the player as a clickable message
        sendCopyableMessage(sender, setblockCommand)

        return true
    }

    private fun sendCopyableMessage(player: Player, text: String) {
        // Using standard Spigot/Bungee API instead of Adventure/Kyori
        val message = TextComponent("[Click to Copy 1.20 Command]")
        message.color = net.md_5.bungee.api.ChatColor.GREEN
        message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)
        message.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Click to copy command to clipboard").create())

        player.sendMessage("${ChatColor.YELLOW}Export Successful! ")
        player.spigot().sendMessage(message)
    }

    /**
     * Generates a 1.20.1 compatible /setblock command string from a 1.21 chest.
     */
    private fun generateLegacyCommand(chest: Chest, block: Block): String {
        val inv = chest.inventory

        // iterate all slots to maintain correct slot indices
        val itemsData = (0 until inv.size).mapNotNull { i ->
            val item = inv.getItem(i)
            if (item == null || item.type == Material.AIR) return@mapNotNull null

            val tag = convertMetaToLegacyNbt(item)
            val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""

            // Construct the NBT object for this item
            "{Slot:${i}b,id:\"minecraft:${item.type.name.lowercase()}\",Count:${item.amount}b$tagString}"
        }

        val itemsNbt = itemsData.joinToString(separator = ",", prefix = "{Items:[", postfix = "]}")

        // Format: setblock x y z minecraft:chest{...} replace
        return "setblock ${block.x} ${block.y} ${block.z} minecraft:chest$itemsNbt replace"
    }

    /**
     * Manually reconstructs 1.20-style NBT from Bukkit ItemMeta.
     */
    private fun convertMetaToLegacyNbt(item: ItemStack): String {
        if (!item.hasItemMeta()) return ""
        val meta = item.itemMeta ?: return ""
        val tagParts = mutableListOf<String>()

        // 1. Display Name
        // Using standard Spigot API (getDisplayName) + Bungee Serializer to get JSON
        if (meta.hasDisplayName()) {
            val legacyName = meta.displayName // returns String with legacy colors
            // Convert legacy text (e.g. §cName) to JSON component string (e.g. {"text":"Name","color":"red"})
            val components = TextComponent.fromLegacyText(legacyName)
            val jsonName = ComponentSerializer.toString(*components)

            tagParts.add("display:{Name:'${escapeNbtString(jsonName)}'}")
        }

        // 2. Lore
        if (meta.hasLore()) {
            val lore = meta.lore
            if (!lore.isNullOrEmpty()) {
                val jsonLines = lore.joinToString(",") { line ->
                    val components = TextComponent.fromLegacyText(line)
                    val jsonLine = ComponentSerializer.toString(*components)
                    "'${escapeNbtString(jsonLine)}'"
                }

                val loreString = "Lore:[$jsonLines]"

                // Merge Name and Lore if needed
                val existingDisplay = tagParts.find { it.startsWith("display:") }

                if (existingDisplay != null) {
                    tagParts.remove(existingDisplay)
                    val nameContent = existingDisplay
                        .removePrefix("display:{")
                        .removeSuffix("}")
                    tagParts.add("display:{$nameContent,$loreString}")
                } else {
                    tagParts.add("display:{$loreString}")
                }
            }
        }

        // 3. Enchantments
        if (meta.hasEnchants()) {
            val enchants = meta.enchants.entries.joinToString(",") { (enchant, level) ->
                val key = enchant.key.key.toString()
                "{id:\"$key\",lvl:${level}s}"
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

    /**
     * Helper to escape single quotes for NBT string validity.
     */
    private fun escapeNbtString(json: String): String {
        return json.replace("'", "\\'")
    }
}