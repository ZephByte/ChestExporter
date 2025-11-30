// language: kotlin
package org.zephbyte.chestExporter

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.plugin.java.JavaPlugin

class ChestExporter : JavaPlugin(), CommandExecutor {

    // Items to ignore when generating legacy /setblock commands
// Items to ignore when generating legacy /setblock commands
    private val backportMap: Map<String, String?> = mapOf(
        "minecraft:armadillo_scute" to null,
        "minecraft:armadillo_spawn_egg" to null,
        "minecraft:black_bundle" to null,
        "minecraft:black_harness" to "dried_ghast:black_harness",
        "minecraft:resin_block" to "palegardenbackport:block_of_resin",
        "minecraft:blue_bundle" to null,
        "minecraft:blue_egg" to null,
        "minecraft:blue_harness" to "dried_ghast:blue_harness",
        "minecraft:bogged_spawn_egg" to "tricky_trials:bogged_spawn_egg",
        "minecraft:bolt_armor_trim_smithing_template" to "tricky_trials:boltarmortrimsmithingtemplate",
        "minecraft:bordure_indented_banner_pattern" to null,
        "minecraft:breeze_rod" to "tricky_trials:breezerod",
        "minecraft:breeze_spawn_egg" to "tricky_trials:breeze_spawn_egg",
        "minecraft:brown_bundle" to null,
        "minecraft:brown_egg" to null,
        "minecraft:brown_harness" to "dried_ghast:brown_harness",
        "minecraft:bundle" to null,
        "minecraft:bush" to null,
        "minecraft:cactus_flower" to null,
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
        "minecraft:cyan_bundle" to null,
        "minecraft:cyan_harness" to "dried_ghast:cyan_harness",
        "minecraft:dried_ghast" to "dried_ghast:dried_ghast",
        "minecraft:exposed_chiseled_copper" to "copperandtuffbackport:exposed_chiseled_copper",
        "minecraft:exposed_copper_bulb" to "copperandtuffbackport:exposed_copper_bulb",
        "minecraft:exposed_copper_door" to "copperandtuffbackport:exposed_copper_door",
        "minecraft:exposed_copper_grate" to "copperandtuffbackport:exposed_copper_grate",
        "minecraft:exposed_copper_trapdoor" to "copperandtuffbackport:exposed_copper_trapdoor",
        "minecraft:field_masoned_banner_pattern" to null,
        "minecraft:firefly_bush" to "fireflybushport:firefly_bush",
        "minecraft:flow_armor_trim_smithing_template" to "tricky_trials:flowarmortrimsmithingtemplate",
        "minecraft:flow_banner_pattern" to null,
        "minecraft:flow_pottery_sherd" to null,
        "minecraft:gray_bundle" to null,
        "minecraft:gray_harness" to "dried_ghast:gray_harness",
        "minecraft:green_bundle" to null,
        "minecraft:green_harness" to "dried_ghast:green_harness",
        "minecraft:guster_banner_pattern" to null,
        "minecraft:guster_pottery_sherd" to null,
        "minecraft:happy_ghast_spawn_egg" to "dried_ghast:happy_ghast_spawn_egg",
        "minecraft:heavy_core" to "tricky_trials:heavycore",
        "minecraft:leaf_litter" to null,
        "minecraft:light_blue_bundle" to null,
        "minecraft:light_blue_harness" to "dried_ghast:light_blue_harness",
        "minecraft:light_gray_bundle" to null,
        "minecraft:light_gray_harness" to "dried_ghast:light_gray_harness",
        "minecraft:lime_bundle" to null,
        "minecraft:lime_harness" to "dried_ghast:lime_harness",
        "minecraft:mace" to "tricky_trials:mace",
        "minecraft:magenta_bundle" to null,
        "minecraft:magenta_harness" to "dried_ghast:magenta_harness",
        "minecraft:music_disc_creator" to "disc_backport_rebelspark:creator_music_disc",
        "minecraft:music_disc_creator_music_box" to "disc_backport_rebelspark:creator_music_box_music_disc",
        "minecraft:music_disc_lava_chicken" to null,
        "minecraft:music_disc_precipice" to "disc_backport_rebelspark:precipice_music_disc",
        "minecraft:music_disc_tears" to "disc_backport_rebelspark:tears_music_disc",
        "minecraft:ominous_bottle" to null,
        "minecraft:ominous_trial_key" to "tricky_trials:ominous_trial_key",
        "minecraft:open_eyeblossom" to "palegardenbackport:open_eyeblossom",
        "minecraft:orange_bundle" to null,
        "minecraft:orange_harness" to "dried_ghast:orange_harness",
        "minecraft:oxidized_chiseled_copper" to "copperandtuffbackport:oxidized_chiseled_copper",
        "minecraft:oxidized_copper_bulb" to "copperandtuffbackport:oxidized_copper_bulb",
        "minecraft:oxidized_copper_door" to "copperandtuffbackport:oxidized_copper_door",
        "minecraft:oxidized_copper_grate" to "copperandtuffbackport:oxidized_copper_grate",
        "minecraft:oxidized_copper_trapdoor" to "copperandtuffbackport:oxidized_copper_trapdoor",
        "minecraft:pale_hanging_moss" to "palegardenbackport:pale_hanging_moss",
        "minecraft:pale_moss_block" to "palegardenbackport:pale_moss_block",
        "minecraft:pale_moss_carpet" to "palegardenbackport:pale_moss_carpet",
        "minecraft:pale_oak_boat" to "palegardenbackport:pale_oak_boat",
        "minecraft:pale_oak_chest_boat" to "palegardenbackport:pale_oak_chest_boat",
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
        "minecraft:pink_bundle" to null,
        "minecraft:pink_harness" to "dried_ghast:pink_harness",
        "minecraft:polished_tuff" to "copperandtuffbackport:polished_tuff",
        "minecraft:polished_tuff_slab" to "copperandtuffbackport:polished_tuff_slab",
        "minecraft:polished_tuff_stairs" to "copperandtuffbackport:polished_tuff_stairs",
        "minecraft:polished_tuff_wall" to "copperandtuffbackport:polished_tuff_wall",
        "minecraft:purple_bundle" to null,
        "minecraft:purple_harness" to "dried_ghast:purple_harness",
        "minecraft:red_bundle" to null,
        "minecraft:red_harness" to "dried_ghast:red_harness",
        "minecraft:resin_brick" to "palegardenbackport:resin_brick",
        "minecraft:resin_brick_slab" to "palegardenbackport:resin_brick_slab",
        "minecraft:resin_brick_stairs" to "palegardenbackport:resin_brick_stairs",
        "minecraft:resin_brick_wall" to "palegardenbackport:resin_brick_wall",
        "minecraft:resin_bricks" to "palegardenbackport:resin_bricks",
        "minecraft:resin_clump" to "palegardenbackport:resin_clump",
        "minecraft:scrape_pottery_sherd" to null,
        "minecraft:short_dry_grass" to null,
        "minecraft:stripped_pale_oak_log" to "palegardenbackport:stripped_pale_oak_log",
        "minecraft:stripped_pale_oak_wood" to "palegardenbackport:stripped_pale_oak_wood",
        "minecraft:tall_dry_grass" to null,
        "minecraft:test_block" to null,
        "minecraft:test_instance_block" to null,
        "minecraft:trial_key" to "tricky_trials:trialkey",
        "minecraft:trial_spawner" to null,
        "minecraft:tuff_brick_slab" to "copperandtuffbackport:tuff_brick_slab",
        "minecraft:tuff_brick_stairs" to "copperandtuffbackport:tuff_brick_stairs",
        "minecraft:tuff_brick_wall" to "copperandtuffbackport:tuff_brick_wall",
        "minecraft:tuff_bricks" to "copperandtuffbackport:tuff_bricks",
        "minecraft:tuff_slab" to "copperandtuffbackport:tuff_slab",
        "minecraft:tuff_stairs" to "copperandtuffbackport:tuff_stairs",
        "minecraft:tuff_wall" to "copperandtuffbackport:tuff_wall",
        "minecraft:vault" to "tricky_trials:the_vault",
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
        "minecraft:white_bundle" to null,
        "minecraft:white_harness" to "dried_ghast:white_harness",
        "minecraft:wildflowers" to null,
        "minecraft:wind_charge" to "tricky_trials:windchargeitem",
        "minecraft:wolf_armor" to null,
        "minecraft:yellow_bundle" to null,
        "minecraft:yellow_harness" to "dried_ghast:yellow_harness"
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

        val chestState = block.state as Chest
        val inventory = chestState.inventory

        sender.sendMessage("${ChatColor.YELLOW}Export Successful!")

        if (inventory is org.bukkit.inventory.DoubleChestInventory) {
            sender.sendMessage("${ChatColor.AQUA}Double chest detected. Generating two commands.")

            val leftChest = inventory.leftSide.holder as Chest
            val rightChest = inventory.rightSide.holder as Chest

            val leftResult = generateSingleChestCommand(leftChest)
            val rightResult = generateSingleChestCommand(rightChest)

            sendCopyableMessage(sender, leftResult.command, "Click to Copy Left Half Command")
            sendCopyableMessage(sender, rightResult.command, "Click to Copy Right Half Command")

            val combinedResult = GenerationResult(
                "", // Not used
                leftResult.ignored + rightResult.ignored,
                leftResult.backported + rightResult.backported
            )
            reportResults(sender, combinedResult)

        } else {
            val result = generateSingleChestCommand(chestState)
            sendCopyableMessage(sender, result.command)
            reportResults(sender, result)
        }

        return true
    }

    private fun reportResults(sender: Player, result: GenerationResult) {
        if (result.backported.isNotEmpty()) {
            val backportedMsg = result.backported.joinToString(",\n") { (id, amount) -> "$id x$amount" }
            sender.sendMessage("${ChatColor.AQUA}Backported: \n$backportedMsg")
        }
        if (result.ignored.isNotEmpty()) {
            val ignoredMsg = result.ignored.joinToString(",\n") { (id, amount) -> "$id x$amount" }
            sender.sendMessage("${ChatColor.RED}Could not move:\n$ignoredMsg")
            logger.info("${sender.name} could not move items: $ignoredMsg")
        }
    }

    private fun sendCopyableMessage(player: Player, text: String, title: String = "[Click to Copy 1.20 Command]") {
        val message = TextComponent(title)
        message.color = net.md_5.bungee.api.ChatColor.GREEN
        message.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)
        message.hoverEvent =
            HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Click to copy command to clipboard").create())
        player.spigot().sendMessage(message)
    }

    private data class GenerationResult(
        val command: String,
        val ignored: List<Pair<String, Int>>,
        val backported: List<Pair<String, Int>>
    )

    /**
     * Generates a 1.20.1 compatible /setblock command string from a single chest.
     * Returns the command and a list of ignored/backported items.
     */
    private fun generateSingleChestCommand(chest: Chest): GenerationResult {
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

            if (backportMap.containsKey(idKey)) {
                val backportId = backportMap[idKey]
                if (backportId != null) {
                    backportedItems.add(Pair(backportId, item.amount))
                    finalId = backportId
                } else {
                    ignoredItems.add(Pair(idKey, item.amount))
                    return@mapNotNull null
                }
            }

            val tag = convertMetaToLegacyNbt(item)
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

    /**
     * Manually reconstructs 1.20-style NBT from Bukkit ItemMeta.
     */
    private fun convertMetaToLegacyNbt(item: ItemStack): String {
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

    /**
     * Helper to escape single quotes for NBT string validity.
     */
    private fun escapeNbtString(json: String): String {
        return json.replace("'", "\\'")
    }
}
