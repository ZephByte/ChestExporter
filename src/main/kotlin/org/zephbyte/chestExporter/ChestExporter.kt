// language: kotlin
package org.zephbyte.chestExporter

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.ChatColor
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

    // map of ignorable -> backport id (if available)
    private val backports: Map<String, String> = mapOf(
        "minecraft:black_harness" to "driedghast:black_harness",
        "minecraft:block_of_resin" to "palegardenbackport:block_of_resin",
        "minecraft:blue_harness" to "driedghast:blue_harness",
        "minecraft:brown_harness" to "driedghast:brown_harness",
        "minecraft:chiseled_copper" to "copperandtuffbackport:chiseled_copper",
        "minecraft:chiseled_resin_bricks" to "palegardenbackport:chiseled_resin_bricks",
        "minecraft:chiseled_tuff" to "copperandtuffbackport:chiseled_tuff",
        "minecraft:chiseled_tuff_bricks" to "copperandtuffbackport:chiseled_tuff_bricks",
        "minecraft:closed_eyeblossom" to "palegardenbackport:closed_eyeblossom",
        "minecraft:copper_bulb" to "copperandtuffbackport:copper_bulb",
        "minecraft:copper_door" to "copperandtuffbackport:copper_door",
        "minecraft:copper_grate" to "copperandtuffbackport:copper_grate",
        "minecraft:copper_trapdoor" to "copperandtuffbackport:copper_trapdoor",
        "minecraft:crafter" to "crafter_port:crafter",
        "minecraft:creaking_heart" to "palegardenbackport:creaking_heart",
        "minecraft:creaking_spawn_egg" to "palegardenbackport:creaking_spawn_egg",
        "minecraft:cyan_harness" to "driedghast:cyan_harness",
        "minecraft:dried_ghast" to "driedghast:dried_ghast",
        "minecraft:exposed_chiseled_copper" to "copperandtuffbackport:exposed_chiseled_copper",
        "minecraft:exposed_copper_bulb" to "copperandtuffbackport:exposed_copper_bulb",
        "minecraft:exposed_copper_door" to "copperandtuffbackport:exposed_copper_door",
        "minecraft:exposed_copper_grate" to "copperandtuffbackport:exposed_copper_grate",
        "minecraft:exposed_copper_trapdoor" to "copperandtuffbackport:exposed_copper_trapdoor",
        "minecraft:firefly_bush" to "fireflybushport:firefly_bush",
        "minecraft:light_blue_harness" to "driedghast:light_blue_harness",
        "minecraft:light_gray_harness" to "driedghast:light_gray_harness",
        "minecraft:lime_harness" to "driedghast:lime_harness",
        "minecraft:magenta_harness" to "driedghast:magenta_harness",
        "minecraft:music_disc_creator" to "disc_backport_rebelspark:creator_music_disc",
        "minecraft:music_disc_creator_music_box" to "disc_backport_rebelspark:creator_music_box_music_disc",
        "minecraft:music_disc_precipice" to "disc_backport_rebelspark:precipice_music_disc",
        "minecraft:music_disc_tears" to "disc_backport_rebelspark:tears_music_disc",
        "minecraft:open_eyeblossom" to "palegardenbackport:open_eyeblossom",
        "minecraft:orange_harness" to "driedghast:orange_harness",
        "minecraft:oxidized_chiseled_copper" to "copperandtuffbackport:oxidized_chiseled_copper",
        "minecraft:oxidized_copper_bulb" to "copperandtuffbackport:oxidized_copper_bulb",
        "minecraft:oxidized_copper_door" to "copperandtuffbackport:oxidized_copper_door",
        "minecraft:oxidized_copper_grate" to "copperandtuffbackport:oxidized_copper_grate",
        "minecraft:oxidized_copper_trapdoor" to "copperandtuffbackport:oxidized_copper_trapdoor",
        "minecraft:pale_hanging_moss" to "palegardenbackport:pale_hanging_moss",
        "minecraft:pale_moss_block" to "palegardenbackport:pale_moss_block",
        "minecraft:pale_moss_carpet" to "palegardenbackport:pale_moss_carpet",
        "minecraft:pale_oak_boat" to "palegardenbackport:pale_oak_boat",
        "minecraft:pale_oak_boat_with_chest" to "palegardenbackport:pale_oak_chest_boat",
        "minecraft:pale_oak_button" to "palegardenbackport:pale_oak_button",
        "minecraft:pale_oak_door" to "palegardenbackport:pale_oak_door",
        "minecraft:pale_oak_fence" to "palegardenbackport:pale_oak_fence",
        "minecraft:pale_oak_fence_gate" to "palegardenbackport:pale_oak_fence_gate",
        "minecraft:pale_oak_hanging_sign" to "palegardenbackport:pale_oak_hanging_sign",
        "minecraft:pale_oak_leaves" to "palegardenbackport:pale_oak_leaves",
        "minecraft:pale_oak_log" to "palegardenbackport:pale_oak_log",
        "minecraft:pale_oak_planks" to "palegardenbackport:pale_oak_planks",
        "minecraft:pale_oak_pressure_plate" to "palegardenbackport:pale_oak_pressure_plate",
        "minecraft:pale_oak_sapling" to "palegardenbackport:pale_oak_sapling",
        "minecraft:pale_oak_sign" to "palegardenbackport:pale_oak_sign",
        "minecraft:pale_oak_slab" to "palegardenbackport:pale_oak_slab",
        "minecraft:pale_oak_stairs" to "palegardenbackport:pale_oak_stairs",
        "minecraft:pale_oak_trapdoor" to "palegardenbackport:pale_oak_trapdoor",
        "minecraft:pale_oak_wood" to "palegardenbackport:pale_oak_wood",
        "minecraft:pink_harness" to "driedghast:pink_harness",
        "minecraft:polished_tuff" to "copperandtuffbackport:polished_tuff",
        "minecraft:polished_tuff_slab" to "copperandtuffbackport:polished_tuff_slab",
        "minecraft:polished_tuff_stairs" to "copperandtuffbackport:polished_tuff_stairs",
        "minecraft:polished_tuff_wall" to "copperandtuffbackport:polished_tuff_wall",
        "minecraft:purple_harness" to "driedghast:purple_harness",
        "minecraft:red_harness" to "driedghast:red_harness",
        "minecraft:resin_brick" to "palegardenbackport:resin_brick",
        "minecraft:resin_brick_slab" to "palegardenbackport:resin_brick_slab",
        "minecraft:resin_brick_stairs" to "palegardenbackport:resin_brick_stairs",
        "minecraft:resin_brick_wall" to "palegardenbackport:resin_brick_wall",
        "minecraft:resin_bricks" to "palegardenbackport:resin_bricks",
        "minecraft:resin_clump" to "palegardenbackport:resin_clump",
        "minecraft:stripped_pale_oak_log" to "palegardenbackport:stripped_pale_oak_log",
        "minecraft:stripped_pale_oak_wood" to "palegardenbackport:stripped_pale_oak_wood",
        "minecraft:tuff_brick_slab" to "copperandtuffbackport:tuff_brick_slab",
        "minecraft:tuff_brick_stairs" to "copperandtuffbackport:tuff_brick_stairs",
        "minecraft:tuff_brick_wall" to "copperandtuffbackport:tuff_brick_wall",
        "minecraft:tuff_bricks" to "copperandtuffbackport:tuff_bricks",
        "minecraft:tuff_slab" to "copperandtuffbackport:tuff_slab",
        "minecraft:tuff_stairs" to "copperandtuffbackport:tuff_stairs",
        "minecraft:tuff_wall" to "copperandtuffbackport:tuff_wall",
        "minecraft:waxed_chiseled_copper" to "copperandtuffbackport:waxed_chiseled_copper",
        "minecraft:waxed_copper_bulb" to "copperandtuffbackport:waxed_copper_bulb",
        "minecraft:waxed_copper_door" to "copperandtuffbackport:waxed_copper_door",
        "minecraft:waxed_copper_grate" to "copperandtuffbackport:waxed_copper_grate",
        "minecraft:waxed_copper_trapdoor" to "copperandtuffbackport:waxed_copper_trapdoor",
        "minecraft:waxed_exposed_chiseled_copper" to "copperandtuffbackport:waxed_exposed_chiseled_copper",
        "minecraft:waxed_exposed_copper_bulb" to "copperandtuffbackport:waxed_exposed_copper_bulb",
        "minecraft:waxed_exposed_copper_door" to "copperandtuffbackport:waxed_exposed_copper_door",
        "minecraft:waxed_exposed_copper_grate" to "copperandtuffbackport:waxed_exposed_copper_grate",
        "minecraft:waxed_exposed_copper_trapdoor" to "copperandtuffbackport:waxed_exposed_copper_trapdoor",
        "minecraft:waxed_oxidized_chiseled_copper" to "copperandtuffbackport:waxed_oxidized_chiseled_copper",
        "minecraft:waxed_oxidized_copper_bulb" to "copperandtuffbackport:waxed_oxidized_copper_bulb",
        "minecraft:waxed_oxidized_copper_door" to "copperandtuffbackport:waxed_oxidized_copper_door",
        "minecraft:waxed_oxidized_copper_grate" to "copperandtuffbackport:waxed_oxidized_copper_grate",
        "minecraft:waxed_oxidized_copper_trapdoor" to "copperandtuffbackport:waxed_oxidized_copper_trapdoor",
        "minecraft:waxed_weathered_chiseled_copper" to "copperandtuffbackport:waxed_weathered_chiseled_copper",
        "minecraft:waxed_weathered_copper_bulb" to "copperandtuffbackport:waxed_weathered_copper_bulb",
        "minecraft:waxed_weathered_copper_door" to "copperandtuffbackport:waxed_weathered_copper_door",
        "minecraft:waxed_weathered_copper_grate" to "copperandtuffbackport:waxed_weathered_copper_grate",
        "minecraft:waxed_weathered_copper_trapdoor" to "copperandtuffbackport:waxed_weathered_copper_trapdoor",
        "minecraft:weathered_chiseled_copper" to "copperandtuffbackport:weathered_chiseled_copper",
        "minecraft:weathered_copper_bulb" to "copperandtuffbackport:weathered_copper_bulb",
        "minecraft:weathered_copper_door" to "copperandtuffbackport:weathered_copper_door",
        "minecraft:weathered_copper_grate" to "copperandtuffbackport:weathered_copper_grate",
        "minecraft:weathered_copper_trapdoor" to "copperandtuffbackport:weathered_copper_trapdoor",
        "minecraft:white_harness" to "driedghast:white_harness",
        "minecraft:yellow_harness" to "driedghast:yellow_harness"
    )

    // Items to ignore when generating legacy /setblock commands
    private val removed: Set<String> = setOf(
        "minecraft:armadillo_scute",
        "minecraft:armadillo_spawn_egg",
        "minecraft:black_bundle",
        "minecraft:black_harness",
        "minecraft:block_of_resin",
        "minecraft:blue_bundle",
        "minecraft:blue_egg",
        "minecraft:blue_harness",
        "minecraft:bogged_spawn_egg",
        "minecraft:bolt_armor_trim_smithing_template",
        "minecraft:bordure_indented_banner_pattern",
        "minecraft:breeze_rod",
        "minecraft:breeze_spawn_egg",
        "minecraft:brown_bundle",
        "minecraft:brown_egg",
        "minecraft:brown_harness",
        "minecraft:bundle",
        "minecraft:bush",
        "minecraft:cactus_flower",
        "minecraft:chiseled_copper",
        "minecraft:chiseled_resin_bricks",
        "minecraft:chiseled_tuff",
        "minecraft:chiseled_tuff_bricks",
        "minecraft:closed_eyeblossom",
        "minecraft:copper_bulb",
        "minecraft:copper_door",
        "minecraft:copper_grate",
        "minecraft:copper_trapdoor",
        "minecraft:crafter",
        "minecraft:creaking_heart",
        "minecraft:creaking_spawn_egg",
        "minecraft:creaking_spawn_eggs",
        "minecraft:cyan_bundle",
        "minecraft:cyan_harness",
        "minecraft:dried_ghast",
        "minecraft:exposed_chiseled_copper",
        "minecraft:exposed_copper_bulb",
        "minecraft:exposed_copper_door",
        "minecraft:exposed_copper_grate",
        "minecraft:exposed_copper_trapdoor",
        "minecraft:field_masoned_banner_pattern",
        "minecraft:firefly_bush",
        "minecraft:flow_armor_trim_smithing_template",
        "minecraft:flow_banner_pattern",
        "minecraft:flow_pottery_sherd",
        "minecraft:gray_bundle",
        "minecraft:gray_harness",
        "minecraft:green_bundle",
        "minecraft:green_harness",
        "minecraft:guster_banner_pattern",
        "minecraft:guster_pottery_sherd",
        "minecraft:happy_ghast_spawn_egg",
        "minecraft:heavy_core",
        "minecraft:leaf_litter",
        "minecraft:light_blue_bundle",
        "minecraft:light_blue_harness",
        "minecraft:light_gray_bundle",
        "minecraft:light_gray_harness",
        "minecraft:lime_bundle",
        "minecraft:lime_harness",
        "minecraft:mace",
        "minecraft:magenta_bundle",
        "minecraft:magenta_harness",
        "minecraft:music_disc_creator",
        "minecraft:music_disc_creator_music_box",
        "minecraft:music_disc_lava_chicken",
        "minecraft:music_disc_precipice",
        "minecraft:music_disc_tears",
        "minecraft:ominous_bottle",
        "minecraft:ominous_trial_key",
        "minecraft:open_eyeblossom",
        "minecraft:orange_bundle",
        "minecraft:orange_harness",
        "minecraft:oxidized_chiseled_copper",
        "minecraft:oxidized_copper_bulb",
        "minecraft:oxidized_copper_door",
        "minecraft:oxidized_copper_grate",
        "minecraft:oxidized_copper_trapdoor",
        "minecraft:pale_hanging_moss",
        "minecraft:pale_moss_block",
        "minecraft:pale_moss_carpet",
        "minecraft:pale_oak_boat",
        "minecraft:pale_oak_boat_with_chest",
        "minecraft:pale_oak_button",
        "minecraft:pale_oak_door",
        "minecraft:pale_oak_fence",
        "minecraft:pale_oak_fence_gate",
        "minecraft:pale_oak_hanging_sign",
        "minecraft:pale_oak_leaves",
        "minecraft:pale_oak_log",
        "minecraft:pale_oak_planks",
        "minecraft:pale_oak_pressure_plate",
        "minecraft:pale_oak_sapling",
        "minecraft:pale_oak_sign",
        "minecraft:pale_oak_slab",
        "minecraft:pale_oak_stairs",
        "minecraft:pale_oak_trapdoor",
        "minecraft:pale_oak_wood",
        "minecraft:pink_bundle",
        "minecraft:pink_harness",
        "minecraft:polished_tuff",
        "minecraft:polished_tuff_slab",
        "minecraft:polished_tuff_stairs",
        "minecraft:polished_tuff_wall",
        "minecraft:purple_bundle",
        "minecraft:purple_harness",
        "minecraft:red_bundle",
        "minecraft:red_harness",
        "minecraft:resin_brick",
        "minecraft:resin_brick_slab",
        "minecraft:resin_brick_stairs",
        "minecraft:resin_brick_wall",
        "minecraft:resin_bricks",
        "minecraft:resin_clump",
        "minecraft:scrape_pottery_sherd",
        "minecraft:short_dry_grass",
        "minecraft:stripped_pale_oak_log",
        "minecraft:stripped_pale_oak_wood",
        "minecraft:tall_dry_grass",
        "minecraft:test_block",
        "minecraft:test_instance_block",
        "minecraft:trial_key",
        "minecraft:trial_spawner",
        "minecraft:tuff_brick_slab",
        "minecraft:tuff_brick_stairs",
        "minecraft:tuff_brick_wall",
        "minecraft:tuff_bricks",
        "minecraft:tuff_slab",
        "minecraft:tuff_stairs",
        "minecraft:tuff_wall",
        "minecraft:vault",
        "minecraft:waxed_chiseled_copper",
        "minecraft:waxed_copper_bulb",
        "minecraft:waxed_copper_door",
        "minecraft:waxed_copper_grate",
        "minecraft:waxed_copper_trapdoor",
        "minecraft:waxed_exposed_chiseled_copper",
        "minecraft:waxed_exposed_copper_bulb",
        "minecraft:waxed_exposed_copper_door",
        "minecraft:waxed_exposed_copper_grate",
        "minecraft:waxed_exposed_copper_trapdoor",
        "minecraft:waxed_oxidized_chiseled_copper",
        "minecraft:waxed_oxidized_copper_bulb",
        "minecraft:waxed_oxidized_copper_door",
        "minecraft:waxed_oxidized_copper_grate",
        "minecraft:waxed_oxidized_copper_trapdoor",
        "minecraft:waxed_weathered_chiseled_copper",
        "minecraft:waxed_weathered_copper_bulb",
        "minecraft:waxed_weathered_copper_door",
        "minecraft:waxed_weathered_copper_grate",
        "minecraft:waxed_weathered_copper_trapdoor",
        "minecraft:weathered_chiseled_copper",
        "minecraft:weathered_copper_bulb",
        "minecraft:weathered_copper_door",
        "minecraft:weathered_copper_grate",
        "minecraft:weathered_copper_trapdoor",
        "minecraft:white_bundle",
        "minecraft:white_harness",
        "minecraft:wildflowers",
        "minecraft:wind_charge",
        "minecraft:wolf_armor",
        "minecraft:yellow_bundle",
        "minecraft:yellow_harness"
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
            sender.sendMessage("${ChatColor.RED}You must be looking at a Chest.")
            return true
        }

        val chest = block.state as Chest
        val result = generateLegacyCommand(chest, block)

        // Send the command to the player as a clickable message
        sendCopyableMessage(sender, result.command)

        // If any items were backported, inform the player
        if (result.backported.isNotEmpty()) {
            val backportedMsg = result.backported.joinToString(", ") { (id, amount) -> "$id x$amount" }
            sender.sendMessage("${ChatColor.AQUA}Backported: $backportedMsg")
        }

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

    private data class GenerationResult(
    val command: String,
    val ignored: List<Pair<String, Int>>,
    val backported: List<Pair<String, Int>>
)

    /**
     * Generates a 1.20.1 compatible /setblock command string from a 1.21 chest.
     * Returns the command and a list of ignored items (id, amount).
     */
    private fun generateLegacyCommand(chest: Chest, block: Block): GenerationResult {
        val inv = chest.inventory
        val ignoredItems = mutableListOf<Pair<String, Int>>()
        val backportedItems = mutableListOf<Pair<String, Int>>()

        val itemsData = (0 until inv.size).mapNotNull { i ->
            val item = inv.getItem(i) ?: return@mapNotNull null
            val idKey = "minecraft:${item.type.name.lowercase()}"

            if (idKey in removed) {
                // If we have a backport mapping, include the backport id instead of skipping
                val bp = backports[idKey]
                if (bp != null) {
                    val tag = convertMetaToLegacyNbt(item)
                    val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""
                    // record backported item for reporting
                    backportedItems.add(Pair(bp, item.amount))
                    return@mapNotNull "{Slot:${i}b,id:\"$bp\",Count:${item.amount}b$tagString}"
                }

                // truly ignored, record and skip
                ignoredItems.add(Pair(idKey, item.amount))
                return@mapNotNull null
            }

            val tag = convertMetaToLegacyNbt(item)
            val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""

            // Construct the NBT object for this item
            "{Slot:${i}b,id:\"$idKey\",Count:${item.amount}b$tagString}"
        }

        val itemsNbt = itemsData.joinToString(separator = ",", prefix = "{Items:[", postfix = "]}")

        // Format: setblock x y z minecraft:chest{...} replace
        val command = "/setblock ${block.x} ${block.y} ${block.z} minecraft:chest$itemsNbt replace"
        return GenerationResult(command, ignoredItems, backportedItems)
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
