package org.zephbyte.chestExporter

import com.electronwill.nightconfig.core.Config
import com.electronwill.nightconfig.core.file.FileConfig
import java.io.File

/**
 * Manages the plugin's configuration using NightConfig.
 *
 * @param dataFolder The plugin's data folder, where the config file is stored.
 */
class ConfigManager(private val dataFolder: File) {
    private val config: FileConfig

    init {
        val configFile = File(dataFolder, "overrides.toml")
        if (!configFile.exists()) {
            dataFolder.mkdirs()
            // Create a default config file
            val defaultConfig = """
                # Item ID Override Map
                #
                # This file allows you to override the item IDs for items being exported.
                # The format is "original_item_id" = "new_item_id".
                #
                # If an item is in this map, its ID will be replaced with the new ID.
                # If an item is not in this map, its original ID will be used.
                # If an item is in this map but the new ID is null (or an empty string),
                # the item will be ignored during export.
                #
                # Example:
                # "minecraft:diamond" = "minecraft:gold_ingot"
                # "minecraft:dirt" = ""
                [overrides]
                "minecraft:armadillo_scute" = ""
                "minecraft:armadillo_spawn_egg" = ""
                "minecraft:black_bundle" = ""
                "minecraft:black_harness" = "dried_ghast:black_harness"
                "minecraft:resin_block" = "palegardenbackport:block_of_resin"
                "minecraft:blue_bundle" = ""
                "minecraft:blue_egg" = ""
                "minecraft:blue_harness" = "dried_ghast:blue_harness"
                "minecraft:bogged_spawn_egg" = "tricky_trials:bogged_spawn_egg"
                "minecraft:bolt_armor_trim_smithing_template" = "tricky_trials:boltarmortrimsmithingtemplate"
                "minecraft:bordure_indented_banner_pattern" = ""
                "minecraft:breeze_rod" = "tricky_trials:breezerod"
                "minecraft:breeze_spawn_egg" = "tricky_trials:breeze_spawn_egg"
                "minecraft:brown_bundle" = ""
                "minecraft:brown_egg" = ""
                "minecraft:brown_harness" = "dried_ghast:brown_harness"
                "minecraft:bundle" = ""
                "minecraft:bush" = ""
                "minecraft:cactus_flower" = ""
                "minecraft:chiseled_copper" = "copperandtuffbackport:chiseled_copper"
                "minecraft:chiseled_resin_bricks" = "palegardenbackport:chiseled_resin_bricks"
                "minecraft:chiseled_tuff" = "copperandtuffbackport:chiseled_tuff"
                "minecraft:chiseled_tuff_bricks" = "copperandtuffbackport:chiseled_tuff_bricks"
                "minecraft:closed_eyeblossom" = "palegardenbackport:closed_eyeblossom"
                "minecraft:copper_bulb" = "copperandtuffbackport:copper_bulb"
                "minecraft:copper_door" = "copperandtuffbackport:copper_door"
                "minecraft:copper_grate" = "copperandtuffbackport:copper_grate"
                "minecraft:copper_trapdoor" = "copperandtuffbackport:copper_trapdoor"
                "minecraft:crafter" = "crafter_port:crafter"
                "minecraft:creaking_heart" = "palegardenbackport:creaking_heart"
                "minecraft:creaking_spawn_egg" = "palegardenbackport:creaking_spawn_egg"
                "minecraft:cyan_bundle" = ""
                "minecraft:cyan_harness" = "dried_ghast:cyan_harness"
                "minecraft:dried_ghast" = "dried_ghast:dried_ghast"
                "minecraft:exposed_chiseled_copper" = "copperandtuffbackport:exposed_chiseled_copper"
                "minecraft:exposed_copper_bulb" = "copperandtuffbackport:exposed_copper_bulb"
                "minecraft:exposed_copper_door" = "copperandtuffbackport:exposed_copper_door"
                "minecraft:exposed_copper_grate" = "copperandtuffbackport:exposed_copper_grate"
                "minecraft:exposed_copper_trapdoor" = "copperandtuffbackport:exposed_copper_trapdoor"
                "minecraft:field_masoned_banner_pattern" = ""
                "minecraft:firefly_bush" = "fireflybushport:firefly_bush"
                "minecraft:flow_armor_trim_smithing_template" = "tricky_trials:flowarmortrimsmithingtemplate"
                "minecraft:flow_banner_pattern" = ""
                "minecraft:flow_pottery_sherd" = ""
                "minecraft:gray_bundle" = ""
                "minecraft:gray_harness" = "dried_ghast:gray_harness"
                "minecraft:green_bundle" = ""
                "minecraft:green_harness" = "dried_ghast:green_harness"
                "minecraft:guster_banner_pattern" = ""
                "minecraft:guster_pottery_sherd" = ""
                "minecraft:happy_ghast_spawn_egg" = "dried_ghast:happy_ghast_spawn_egg"
                "minecraft:heavy_core" = "tricky_trials:heavycore"
                "minecraft:leaf_litter" = "spring_to_life:leaf_litter"
                "minecraft:light_blue_bundle" = ""
                "minecraft:light_blue_harness" = "dried_ghast:light_blue_harness"
                "minecraft:light_gray_bundle" = ""
                "minecraft:light_gray_harness" = "dried_ghast:light_gray_harness"
                "minecraft:lime_bundle" = ""
                "minecraft:lime_harness" = "dried_ghast:lime_harness"
                "minecraft:mace" = "tricky_trials:mace"
                "minecraft:magenta_bundle" = ""
                "minecraft:magenta_harness" = "dried_ghast:magenta_harness"
                "minecraft:music_disc_creator" = "disc_backport_rebelspark:creator_music_disc"
                "minecraft:music_disc_creator_music_box" = "disc_backport_rebelspark:creator_music_box_music_disc"
                "minecraft:music_disc_lava_chicken" = ""
                "minecraft:music_disc_precipice" = "disc_backport_rebelspark:precipice_music_disc"
                "minecraft:music_disc_tears" = "disc_backport_rebelspark:tears_music_disc"
                "minecraft:ominous_bottle" = ""
                "minecraft:ominous_trial_key" = "tricky_trials:ominous_trial_key"
                "minecraft:open_eyeblossom" = "palegardenbackport:open_eyeblossom"
                "minecraft:orange_bundle" = ""
                "minecraft:orange_harness" = "dried_ghast:orange_harness"
                "minecraft:oxidized_chiseled_copper" = "copperandtuffbackport:oxidized_chiseled_copper"
                "minecraft:oxidized_copper_bulb" = "copperandtuffbackport:oxidized_copper_bulb"
                "minecraft:oxidized_copper_door" = "copperandtuffbackport:oxidized_copper_door"
                "minecraft:oxidized_copper_grate" = "copperandtuffbackport:oxidized_copper_grate"
                "minecraft:oxidized_copper_trapdoor" = "copperandtuffbackport:oxidized_copper_trapdoor"
                "minecraft:pale_hanging_moss" = "palegardenbackport:pale_hanging_moss"
                "minecraft:pale_moss_block" = "palegardenbackport:pale_moss_block"
                "minecraft:pale_moss_carpet" = "palegardenbackport:pale_moss_carpet"
                "minecraft:pale_oak_boat" = "palegardenbackport:pale_oak_boat"
                "minecraft:pale_oak_chest_boat" = "palegardenbackport:pale_oak_chest_boat"
                "minecraft:pale_oak_button" = "palegardenbackport:pale_oak_button"
                "minecraft:pale_oak_door" = "palegardenbackport:pale_oak_door"
                "minecraft:pale_oak_fence" = "palegardenbackport:pale_oak_fence"
                "minecraft:pale_oak_fence_gate" = "palegardenbackport:pale_oak_fence_gate"
                "minecraft:pale_oak_hanging_sign" = "palegardenbackport:pale_oak_hanging_sign"
                "minecraft:pale_oak_leaves" = "palegardenbackport:pale_oak_leaves"
                "minecraft:pale_oak_log" = "palegardenbackport:pale_oak_log"
                "minecraft:pale_oak_planks" = "palegardenbackport:pale_oak_planks"
                "minecraft:pale_oak_pressure_plate" = "palegardenbackport:pale_oak_pressure_plate"
                "minecraft:pale_oak_sapling" = "palegardenbackport:pale_oak_sapling"
                "minecraft:pale_oak_sign" = "palegardenbackport:pale_oak_sign"
                "minecraft:pale_oak_slab" = "palegardenbackport:pale_oak_slab"
                "minecraft:pale_oak_stairs" = "palegardenbackport:pale_oak_stairs"
                "minecraft:pale_oak_trapdoor" = "palegardenbackport:pale_oak_trapdoor"
                "minecraft:pale_oak_wood" = "palegardenbackport:pale_oak_wood"
                "minecraft:pink_bundle" = ""
                "minecraft:pink_harness" = "dried_ghast:pink_harness"
                "minecraft:polished_tuff" = "copperandtuffbackport:polished_tuff"
                "minecraft:polished_tuff_slab" = "copperandtuffbackport:polished_tuff_slab"
                "minecraft:polished_tuff_stairs" = "copperandtuffbackport:polished_tuff_stairs"
                "minecraft:polished_tuff_wall" = "copperandtuffbackport:polished_tuff_wall"
                "minecraft:purple_bundle" = ""
                "minecraft:purple_harness" = "dried_ghast:purple_harness"
                "minecraft:red_bundle" = ""
                "minecraft:red_harness" = "dried_ghast:red_harness"
                "minecraft:resin_brick" = "palegardenbackport:resin_brick"
                "minecraft:resin_brick_slab" = "palegardenbackport:resin_brick_slab"
                "minecraft:resin_brick_stairs" = "palegardenbackport:resin_brick_stairs"
                "minecraft:resin_brick_wall" = "palegardenbackport:resin_brick_wall"
                "minecraft:resin_bricks" = "palegardenbackport:resin_bricks"
                "minecraft:resin_clump" = "palegardenbackport:resin_clump"
                "minecraft:scrape_pottery_sherd" = ""
                "minecraft:short_dry_grass" = ""
                "minecraft:stripped_pale_oak_log" = "palegardenbackport:stripped_pale_oak_log"
                "minecraft:stripped_pale_oak_wood" = "palegardenbackport:stripped_pale_oak_wood"
                "minecraft:tall_dry_grass" = ""
                "minecraft:test_block" = ""
                "minecraft:test_instance_block" = ""
                "minecraft:trial_key" = "tricky_trials:trialkey"
                "minecraft:trial_spawner" = ""
                "minecraft:tuff_brick_slab" = "copperandtuffbackport:tuff_brick_slab"
                "minecraft:tuff_brick_stairs" = "copperandtuffbackport:tuff_brick_stairs"
                "minecraft:tuff_brick_wall" = "copperandtuffbackport:tuff_brick_wall"
                "minecraft:tuff_bricks" = "copperandtuffbackport:tuff_bricks"
                "minecraft:tuff_slab" = "copperandtuffbackport:tuff_slab"
                "minecraft:tuff_stairs" = "copperandtuffbackport:tuff_stairs"
                "minecraft:tuff_wall" = "copperandtuffbackport:tuff_wall"
                "minecraft:vault" = "tricky_trials:the_vault"
                "minecraft:waxed_chiseled_copper" = "copperandtuffbackport:waxed_chiseled_copper"
                "minecraft:waxed_copper_bulb" = "copperandtuffbackport:waxed_copper_bulb"
                "minecraft:waxed_copper_door" = "copperandtuffbackport:waxed_copper_door"
                "minecraft:waxed_copper_grate" = "copperandtuffbackport:waxed_copper_grate"
                "minecraft:waxed_copper_trapdoor" = "copperandtuffbackport:waxed_copper_trapdoor"
                "minecraft:waxed_exposed_chiseled_copper" = "copperandtuffbackport:waxed_exposed_chiseled_copper"
                "minecraft:waxed_exposed_copper_bulb" = "copperandtuffbackport:waxed_exposed_copper_bulb"
                "minecraft:waxed_exposed_copper_door" = "copperandtuffbackport:waxed_exposed_copper_door"
                "minecraft:waxed_exposed_copper_grate" = "copperandtuffbackport:waxed_exposed_copper_grate"
                "minecraft:waxed_exposed_copper_trapdoor" = "copperandtuffbackport:waxed_exposed_copper_trapdoor"
                "minecraft:waxed_oxidized_chiseled_copper" = "copperandtuffbackport:waxed_oxidized_chiseled_copper"
                "minecraft:waxed_oxidized_copper_bulb" = "copperandtuffbackport:waxed_oxidized_copper_bulb"
                "minecraft:waxed_oxidized_copper_door" = "copperandtuffbackport:waxed_oxidized_copper_door"
                "minecraft:waxed_oxidized_copper_grate" = "copperandtuffbackport:waxed_oxidized_copper_grate"
                "minecraft:waxed_oxidized_copper_trapdoor" = "copperandtuffbackport:waxed_oxidized_copper_trapdoor"
                "minecraft:waxed_weathered_chiseled_copper" = "copperandtuffbackport:waxed_weathered_chiseled_copper"
                "minecraft:waxed_weathered_copper_bulb" = "copperandtuffbackport:waxed_weathered_copper_bulb"
                "minecraft:waxed_weathered_copper_door" = "copperandtuffbackport:waxed_weathered_copper_door"
                "minecraft:waxed_weathered_copper_grate" = "copperandtuffbackport:waxed_weathered_copper_grate"
                "minecraft:waxed_weathered_copper_trapdoor" = "copperandtuffbackport:waxed_weathered_copper_trapdoor"
                "minecraft:weathered_chiseled_copper" = "copperandtuffbackport:weathered_chiseled_copper"
                "minecraft:weathered_copper_bulb" = "copperandtuffbackport:weathered_copper_bulb"
                "minecraft:weathered_copper_door" = "copperandtuffbackport:weathered_copper_door"
                "minecraft:weathered_copper_grate" = "copperandtuffbackport:weathered_copper_grate"
                "minecraft:weathered_copper_trapdoor" = "copperandtuffbackport:weathered_copper_trapdoor"
                "minecraft:white_bundle" = ""
                "minecraft:white_harness" = "dried_ghast:white_harness"
                "minecraft:wildflowers" = "spring_to_life:wildflowers"
                "minecraft:wind_charge" = "tricky_trials:windchargeitem"
                "minecraft:wolf_armor" = ""
                "minecraft:yellow_bundle" = ""
                "minecraft:yellow_harness" = "dried_ghast:yellow_harness"
            """.trimIndent()
            configFile.writeText(defaultConfig)
        }
        config = FileConfig.of(configFile)
        config.load()
    }

    /**
     * Gets the override map from the configuration.
     *
     * @return A map of original item IDs to their overridden IDs.
     */
    fun getOverrideMap(): Map<String, String?> {
        val overrides = config.getOptional<Config>("overrides")
        if (overrides.isPresent) {
            // valueMap() returns a Map<String, Any>, so we need to convert the values
            return overrides.get().valueMap().mapValues { (_, value) ->
                val strValue = value as? String
                // Treat blank strings as null, which signifies an ignored item
                if (strValue.isNullOrBlank()) null else strValue
            }
        }
        return emptyMap()
    }
}
