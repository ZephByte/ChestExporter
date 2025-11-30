// language: kotlin
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

    private val backports: Set<String> = setOf(
        ""
    )

    // Items to ignore when generating legacy /setblock commands
// Items to ignore when generating legacy /setblock commands
    private val ignorables: Set<String> = setOf(
        "minecraft:armadillo_scute",
        "minecraft:armadillo_spawn_egg",
        "minecraft:black_bundle",
        "minecraft:black_harness", // driedghast:black_harness
        "minecraft:block_of_resin", // palegardenbackport:block_of_resin
        "minecraft:blue_bundle",
        "minecraft:blue_egg",
        "minecraft:blue_harness", // driedghast:blue_harness
        "minecraft:bogged_spawn_egg",
        "minecraft:bolt_armor_trim_smithing_template",
        "minecraft:bordure_indented_banner_pattern",
        "minecraft:breeze_rod",
        "minecraft:breeze_spawn_egg",
        "minecraft:brown_bundle",
        "minecraft:brown_egg",
        "minecraft:brown_harness", // driedghast:brown_harness
        "minecraft:bundle",
        "minecraft:bush",
        "minecraft:cactus_flower",
        "minecraft:chiseled_copper", // copperandtuffbackport:chiseled_copper
        "minecraft:chiseled_resin_bricks", // palegardenbackport:chiseled_resin_bricks
        "minecraft:chiseled_tuff", // copperandtuffbackport:chiseled_tuff
        "minecraft:chiseled_tuff_bricks", // copperandtuffbackport:chiseled_tuff_bricks
        "minecraft:closed_eyeblossom", // palegardenbackport:closed_eyeblossom
        "minecraft:copper_bulb", // copperandtuffbackport:copper_bulb
        "minecraft:copper_door", // copperandtuffbackport:copper_door
        "minecraft:copper_grate", // copperandtuffbackport:copper_grate
        "minecraft:copper_trapdoor", // copperandtuffbackport:copper_trapdoor
        "minecraft:crafter", //crafter_port:crafter
        "minecraft:creaking_heart", // palegardenbackport:creaking_heart
        "minecraft:creaking_spawn_egg",
        "minecraft:creaking_spawn_eggs", // palegardenbackport:creaking_spawn_eggs
        "minecraft:cyan_bundle",
        "minecraft:cyan_harness", // driedghast:cyan_harness
        "minecraft:dried_ghast", // driedghast:dried_ghast
        "minecraft:exposed_chiseled_copper", // copperandtuffbackport:exposed_chiseled_copper
        "minecraft:exposed_copper_bulb", // copperandtuffbackport:exposed_copper_bulb
        "minecraft:exposed_copper_door", // copperandtuffbackport:exposed_copper_door
        "minecraft:exposed_copper_grate", // copperandtuffbackport:exposed_copper_grate
        "minecraft:exposed_copper_trapdoor", // copperandtuffbackport:exposed_copper_trapdoor
        "minecraft:field_masoned_banner_pattern",
        "minecraft:firefly_bush", // fireflybushport:firefly_bush
        "minecraft:flow_armor_trim_smithing_template",
        "minecraft:flow_banner_pattern",
        "minecraft:flow_pottery_sherd",
        "minecraft:gray_bundle",
        "minecraft:gray_harness", // driedghast:gray_harness
        "minecraft:green_bundle",
        "minecraft:green_harness", // driedghast:green_harness
        "minecraft:guster_banner_pattern",
        "minecraft:guster_pottery_sherd",
        "minecraft:happy_ghast_spawn_egg", // driedghast:happy_ghast_spawn_egg
        "minecraft:heavy_core",
        "minecraft:leaf_litter",
        "minecraft:light_blue_bundle",
        "minecraft:light_blue_harness", // driedghast:light_blue_harness
        "minecraft:light_gray_bundle",
        "minecraft:light_gray_harness", // driedghast:light_gray_harness
        "minecraft:lime_bundle",
        "minecraft:lime_harness", // driedghast:lime_harness
        "minecraft:mace",
        "minecraft:magenta_bundle",
        "minecraft:magenta_harness", // driedghast:magenta_harness
        "minecraft:music_disc_creator", // disc_backport_rebelspark:creator_music_disc
        "minecraft:music_disc_creator_music_box", // disc_backport_rebelspark:creator_music_box_music_disc
        "minecraft:music_disc_lava_chicken",
        "minecraft:music_disc_precipice", // disc_backport_rebelspark:precipice_music_disc
        "minecraft:music_disc_tears", // disc_backport_rebelspark:tears_music_disc
        "minecraft:ominous_bottle",
        "minecraft:ominous_trial_key",
        "minecraft:open_eyeblossom", // palegardenbackport:open_eyeblossom
        "minecraft:orange_bundle",
        "minecraft:orange_harness", // driedghast:orange_harness
        "minecraft:oxidized_chiseled_copper", // copperandtuffbackport:oxidized_chiseled_copper
        "minecraft:oxidized_copper_bulb", // copperandtuffbackport:oxidized_copper_bulb
        "minecraft:oxidized_copper_door", // copperandtuffbackport:oxidized_copper_door
        "minecraft:oxidized_copper_grate", // copperandtuffbackport:oxidized_copper_grate
        "minecraft:oxidized_copper_trapdoor", // copperandtuffbackport:oxidized_copper_trapdoor
        "minecraft:pale_hanging_moss", // palegardenbackport:pale_hanging_moss
        "minecraft:pale_moss_block", // palegardenbackport:pale_moss_block
        "minecraft:pale_moss_carpet", // palegardenbackport:pale_moss_carpet
        "minecraft:pale_oak_boat", // palegardenbackport:pale_oak_boat
        "minecraft:pale_oak_boat_with_chest", // palegardenbackport:pale_oak_chest_boat
        "minecraft:pale_oak_button", // palegardenbackport:pale_oak_button
        "minecraft:pale_oak_door", // palegardenbackport:pale_oak_door
        "minecraft:pale_oak_fence", // palegardenbackport:pale_oak_fence
        "minecraft:pale_oak_fence_gate", // palegardenbackport:pale_oak_fence_gate
        "minecraft:pale_oak_hanging_sign", // palegardenbackport:pale_oak_hanging_sign
        "minecraft:pale_oak_leaves", // palegardenbackport:pale_oak_leaves
        "minecraft:pale_oak_log", // palegardenbackport:pale_oak_log
        "minecraft:pale_oak_planks", // palegardenbackport:pale_oak_planks
        "minecraft:pale_oak_pressure_plate", // palegardenbackport:pale_oak_pressure_plate
        "minecraft:pale_oak_sapling", // palegardenbackport:pale_oak_sapling
        "minecraft:pale_oak_sign", // palegardenbackport:pale_oak_sign
        "minecraft:pale_oak_slab", // palegardenbackport:pale_oak_slab
        "minecraft:pale_oak_stairs", // palegardenbackport:pale_oak_stairs
        "minecraft:pale_oak_trapdoor", // palegardenbackport:pale_oak_trapdoor
        "minecraft:pale_oak_wood", // palegardenbackport:pale_oak_wood
        "minecraft:pink_bundle",
        "minecraft:pink_harness", // driedghast:pink_harness
        "minecraft:polished_tuff", // copperandtuffbackport:polished_tuff
        "minecraft:polished_tuff_slab", // copperandtuffbackport:polished_tuff_slab
        "minecraft:polished_tuff_stairs", // copperandtuffbackport:polished_tuff_stairs
        "minecraft:polished_tuff_wall", // copperandtuffbackport:polished_tuff_wall
        "minecraft:purple_bundle",
        "minecraft:purple_harness", // driedghast:purple_harness
        "minecraft:red_bundle",
        "minecraft:red_harness", // driedghast:red_harness
        "minecraft:resin_brick", // palegardenbackport:resin_brick
        "minecraft:resin_brick_slab", // palegardenbackport:resin_brick_slab
        "minecraft:resin_brick_stairs", // palegardenbackport:resin_brick_stairs
        "minecraft:resin_brick_wall", // palegardenbackport:resin_brick_wall
        "minecraft:resin_bricks", // palegardenbackport:resin_bricks
        "minecraft:resin_clump", // palegardenbackport:resin_clump
        "minecraft:scrape_pottery_sherd",
        "minecraft:short_dry_grass",
        "minecraft:stripped_pale_oak_log", // palegardenbackport:stripped_pale_oak_log
        "minecraft:stripped_pale_oak_wood", // palegardenbackport:stripped_pale_oak_wood
        "minecraft:tall_dry_grass",
        "minecraft:test_block",
        "minecraft:test_instance_block",
        "minecraft:trial_key",
        "minecraft:trial_spawner",
        "minecraft:tuff_brick_slab", // copperandtuffbackport:tuff_brick_slab
        "minecraft:tuff_brick_stairs", // copperandtuffbackport:tuff_brick_stairs
        "minecraft:tuff_brick_wall", // copperandtuffbackport:tuff_brick_wall
        "minecraft:tuff_bricks", // copperandtuffbackport:tuff_bricks
        "minecraft:tuff_slab", // copperandtuffbackport:tuff_slab
        "minecraft:tuff_stairs", // copperandtuffbackport:tuff_stairs
        "minecraft:tuff_wall", // copperandtuffbackport:tuff_wall
        "minecraft:vault",
        "minecraft:waxed_chiseled_copper", // copperandtuffbackport:waxed_chiseled_copper
        "minecraft:waxed_copper_bulb", // copperandtuffbackport:waxed_copper_bulb
        "minecraft:waxed_copper_door", // copperandtuffbackport:waxed_copper_door
        "minecraft:waxed_copper_grate", // copperandtuffbackport:waxed_copper_grate
        "minecraft:waxed_copper_trapdoor", // copperandtuffbackport:waxed_copper_trapdoor
        "minecraft:waxed_exposed_chiseled_copper", // copperandtuffbackport:waxed_exposed_chiseled_copper
        "minecraft:waxed_exposed_copper_bulb", // copperandtuffbackport:waxed_exposed_copper_bulb
        "minecraft:waxed_exposed_copper_door", // copperandtuffbackport:waxed_exposed_copper_door
        "minecraft:waxed_exposed_copper_grate", // copperandtuffbackport:waxed_exposed_copper_grate
        "minecraft:waxed_exposed_copper_trapdoor", // copperandtuffbackport:waxed_exposed_copper_trapdoor
        "minecraft:waxed_oxidized_chiseled_copper", // copperandtuffbackport:waxed_oxidized_chiseled_copper
        "minecraft:waxed_oxidized_copper_bulb", // copperandtuffbackport:waxed_oxidized_copper_bulb
        "minecraft:waxed_oxidized_copper_door", // copperandtuffbackport:waxed_oxidized_copper_door
        "minecraft:waxed_oxidized_copper_grate", // copperandtuffbackport:waxed_oxidized_copper_grate
        "minecraft:waxed_oxidized_copper_trapdoor", // copperandtuffbackport:waxed_oxidized_copper_trapdoor
        "minecraft:waxed_weathered_chiseled_copper", // copperandtuffbackport:waxed_weathered_chiseled_copper
        "minecraft:waxed_weathered_copper_bulb", // copperandtuffbackport:waxed_weathered_copper_bulb
        "minecraft:waxed_weathered_copper_door", // copperandtuffbackport:waxed_weathered_copper_door
        "minecraft:waxed_weathered_copper_grate", // copperandtuffbackport:waxed_weathered_copper_grate
        "minecraft:waxed_weathered_copper_trapdoor", // copperandtuffbackport:waxed_weathered_copper_trapdoor
        "minecraft:weathered_chiseled_copper", // copperandtuffbackport:weathered_chiseled_copper
        "minecraft:weathered_copper_bulb", // copperandtuffbackport:weathered_copper_bulb
        "minecraft:weathered_copper_door", // copperandtuffbackport:weathered_copper_door
        "minecraft:weathered_copper_grate", // copperandtuffbackport:weathered_copper_grate
        "minecraft:weathered_copper_trapdoor", // copperandtuffbackport:weathered_copper_trapdoor
        "minecraft:white_bundle",
        "minecraft:white_harness", // driedghast:white_harness
        "minecraft:wildflowers",
        "minecraft:wind_charge",
        "minecraft:wolf_armor",
        "minecraft:yellow_bundle",
        "minecraft:yellow_harness" // driedghast:yellow_harness
    )

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
        val result = generateLegacyCommand(chest, block)

        // Send the command to the player as a clickable message
        sendCopyableMessage(sender, result.command)

        // If any items were ignored, inform the player and log who triggered it
        if (result.ignored.isNotEmpty()) {
            val ignoredMsg = result.ignored.joinToString(", ") { (id, amount) -> "$id x$amount" }
            sender.sendMessage("${ChatColor.YELLOW}Could not move: $ignoredMsg")
            logger.info("${sender.name} Could not move items: $ignoredMsg")
        }

        return true
    }

    private fun sendCopyableMessage(player: Player, text: String) {
        // Using standard Spigot/Bungee API instead of Adventure/Kyori
        val message = TextComponent("[Click to Copy 1.20 Command]")
        message.color = net.md_5.bungee.api.ChatColor.GREEN
        message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)
        message.hoverEvent =
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Click to copy command to clipboard").create())

        player.sendMessage("${ChatColor.YELLOW}Export Successful! ")
        player.spigot().sendMessage(message)
    }

    private data class GenerationResult(val command: String, val ignored: List<Pair<String, Int>>)

    /**
     * Generates a 1.20.1 compatible /setblock command string from a 1.21 chest.
     * Returns the command and a list of ignored items (id, amount).
     */
    private fun generateLegacyCommand(chest: Chest, block: Block): GenerationResult {
        val inv = chest.inventory

        val ignoredItems = mutableListOf<Pair<String, Int>>()

        // iterate all slots to maintain correct slot indices
        val itemsData = (0 until inv.size).mapNotNull { i ->
            val item = inv.getItem(i)
            if (item == null || item.type == Material.AIR) return@mapNotNull null

            // Determine the minecraft id string used for comparison
            val idKey = "minecraft:${item.type.name.lowercase()}"
            if (idKey in ignorables) {
                // record ignored item and skip it
                ignoredItems.add(Pair(idKey, item.amount))
                return@mapNotNull null
            }

            val tag = convertMetaToLegacyNbt(item)
            val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""

            // Construct the NBT object for this item
            "{Slot:${i}b,id:\"minecraft:${item.type.name.lowercase()}\",Count:${item.amount}b$tagString}"
        }

        val itemsNbt = itemsData.joinToString(separator = ",", prefix = "{Items:[", postfix = "]}")

        // Format: setblock x y z minecraft:chest{...} replace
        val command = "/setblock ${block.x} ${block.y} ${block.z} minecraft:chest$itemsNbt replace"
        return GenerationResult(command, ignoredItems)
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
            // Convert legacy text (e.g. §cName) to JSON component string (e.g. {\"text\":\"Name\",\"color\":\"red\"})
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
