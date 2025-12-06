package org.zephbyte.chestExporter

import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

/**
 * Generates commands to recreate a container or entity with its items in a legacy Minecraft version.
 *
 * @param configManager The configuration manager for the plugin.
 */
class CommandGenerator(private val configManager: ConfigManager) {
    /**
     * Represents the result of a command generation, including the command itself,
     * and lists of items that were ignored or backported.
     */
    data class GenerationResult(
        val command: String,
        val ignored: List<Pair<String, Int>>,
        val backported: List<Pair<String, Int>>
    )

    private val ignoredItems = mutableListOf<Pair<String, Int>>()
    private val backportedItems = mutableListOf<Pair<String, Int>>()

    /**
     * Converts a single ItemStack into its core NBT data string for legacy commands.
     * Handles backporting and tracks ignored/backported items.
     *
     * @param item The ItemStack to process.
     * @param quoteId Whether the final item ID should be wrapped in quotes.
     * @return The NBT string for the item's data, or null if the item should be ignored.
     */
    private fun buildItemNbt(item: ItemStack, quoteId: Boolean): String? {
        val overrideMap = configManager.getOverrideMap()
        val idKey = "minecraft:${item.type.name.lowercase()}"
        var finalId = idKey

        if (overrideMap.containsKey(idKey)) {
            val overrideId = overrideMap[idKey]
            if (overrideId != null && overrideId.isNotBlank()) {
                backportedItems.add(Pair(overrideId, item.amount))
                finalId = overrideId
            } else {
                ignoredItems.add(Pair(idKey, item.amount))
                return null
            }
        }

        val legacyId = finalId.removePrefix("minecraft:")
        val idString = if (quoteId) "\"$legacyId\"" else legacyId

        val tag = NbtConverter.convertMetaToLegacyNbt(item)
        val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""
        return "id:$idString,Count:${item.amount}b$tagString"
    }

    /**
     * Generates a /setblock command for the given container.
     *
     * @param container The container state to generate the command for.
     * @param inventory The specific inventory to serialize (e.g., one half of a double chest).
     * @return A [GenerationResult] containing the command and lists of ignored and backported items.
     */
    fun generateContainerCommand(container: Container, inventory: Inventory): GenerationResult {
        ignoredItems.clear()
        backportedItems.clear()

        val block = container.block
        val blockData = container.blockData
        val blockStateStr = blockData.asString.replaceFirst("minecraft:", "")

        val itemsData = (0 until inventory.size).mapNotNull { i ->
            val item = inventory.getItem(i) ?: return@mapNotNull null
            // Block entities require unquoted IDs
            val itemNbt = buildItemNbt(item, quoteId = false) ?: return@mapNotNull null
            "{Slot:${i}b,$itemNbt}"
        }

        val itemsNbt = if (itemsData.isNotEmpty()) {
            itemsData.joinToString(separator = ",", prefix = "{Items:[", postfix = "]}")
        } else {
            ""
        }

        val command = "/setblock ${block.x} ${block.y} ${block.z} $blockStateStr$itemsNbt replace"
        return GenerationResult(command, ignoredItems.toList(), backportedItems.toList())
    }

    /**
     * Generates a /summon command for the given ArmorStand.
     *
     * @param armorStand The ArmorStand to generate the command for.
     * @return A [GenerationResult] containing the command and lists of ignored and backported items.
     */
    fun generateArmorStandCommand(armorStand: ArmorStand): GenerationResult {
        ignoredItems.clear()
        backportedItems.clear()

        val nbtParts = mutableListOf<String>()

        // Basic properties
        if (armorStand.isSmall) nbtParts.add("Small:1b")
        //if (!armorStand.isBasePlateVisible) nbtParts.add("NoBasePlate:1b")
        if (armorStand.hasArms()) nbtParts.add("ShowArms:1b")
        if (!armorStand.hasGravity()) nbtParts.add("NoGravity:1b")
        if (armorStand.isInvisible) nbtParts.add("Invisible:1b")
        if (armorStand.isCustomNameVisible) nbtParts.add("CustomNameVisible:1b")
        armorStand.customName?.let { nbtParts.add("CustomName:'{\"text\":\"${it.replace("'", "\\'")}\"}'") }

        // Rotation
        val loc = armorStand.location
        nbtParts.add("Rotation:[${loc.yaw}f,${loc.pitch}f]")

        // Equipment - Entities require quoted IDs
        val equipment = armorStand.equipment
        if (equipment != null) {
            val handItems = listOf(equipment.itemInMainHand, equipment.itemInOffHand)
                .mapNotNull { item -> item.takeIf { it.type != Material.AIR }?.let { buildItemNbt(it, quoteId = true) } }
                .joinToString(",") { "{$it}" }
            if (handItems.isNotEmpty()) nbtParts.add("HandItems:[$handItems]")

            val armorItems = equipment.armorContents // Order is boots, leggings, chest, helmet
                .mapNotNull { item -> item.takeIf { it?.type != Material.AIR }?.let { buildItemNbt(it, quoteId = true) } }
                .joinToString(",") { "{$it}" }
            if (armorItems.isNotEmpty()) nbtParts.add("ArmorItems:[$armorItems]")
        }

        // Pose
        val poseParts = mutableListOf<String>()
        val head = armorStand.headPose
        poseParts.add("Head:[${Math.toDegrees(head.x).toFloat()}f,${Math.toDegrees(head.y).toFloat()}f,${Math.toDegrees(head.z).toFloat()}f]")
        val body = armorStand.bodyPose
        poseParts.add("Body:[${Math.toDegrees(body.x).toFloat()}f,${Math.toDegrees(body.y).toFloat()}f,${Math.toDegrees(body.z).toFloat()}f]")
        val leftArm = armorStand.leftArmPose
        poseParts.add("LeftArm:[${Math.toDegrees(leftArm.x).toFloat()}f,${Math.toDegrees(leftArm.y).toFloat()}f,${Math.toDegrees(leftArm.z).toFloat()}f]")
        val rightArm = armorStand.rightArmPose
        poseParts.add("RightArm:[${Math.toDegrees(rightArm.x).toFloat()}f,${Math.toDegrees(rightArm.y).toFloat()}f,${Math.toDegrees(rightArm.z).toFloat()}f]")
        val leftLeg = armorStand.leftLegPose
        poseParts.add("LeftLeg:[${Math.toDegrees(leftLeg.x).toFloat()}f,${Math.toDegrees(leftLeg.y).toFloat()}f,${Math.toDegrees(leftLeg.z).toFloat()}f]")
        val rightLeg = armorStand.rightLegPose
        poseParts.add("RightLeg:[${Math.toDegrees(rightLeg.x).toFloat()}f,${Math.toDegrees(rightLeg.y).toFloat()}f,${Math.toDegrees(rightLeg.z).toFloat()}f]")
        nbtParts.add("Pose:{${poseParts.joinToString(",")}}")


        val nbt = nbtParts.joinToString(",", prefix = "{", postfix = "}")
        val x = armorStand.location.x.roundToInt()
        val y = armorStand.location.y.roundToInt()
        val z = armorStand.location.z.roundToInt()
        val command = "/summon armor_stand $x $y $z $nbt"

        return GenerationResult(command, ignoredItems.toList(), backportedItems.toList())
    }

    /**
     * Generates a /setblock command to recreate a player's inventory inside a chest.
     *
     * @param player The player whose inventory is being exported.
     * @return A [GenerationResult] containing the command and lists of ignored and backported items.
     */
    fun generatePlayerInventoryCommand(player: Player): GenerationResult {
        ignoredItems.clear()
        backportedItems.clear()

        val itemsData = (0..26).mapNotNull { chestSlot ->
            val playerInvSlot = if (chestSlot < 9) chestSlot else chestSlot + 9
            val item = player.inventory.getItem(playerInvSlot) ?: return@mapNotNull null
            // Block entities require unquoted IDs
            val itemNbt = buildItemNbt(item, quoteId = false) ?: return@mapNotNull null
            "{Slot:${chestSlot}b,$itemNbt}"
        }

        val itemsNbt = if (itemsData.isNotEmpty()) {
            itemsData.joinToString(separator = ",", prefix = "{Items:[", postfix = "]}")
        } else {
            ""
        }

        val loc = player.location
        val command = "/setblock ${loc.blockX} ${loc.blockY} ${loc.blockZ} chest$itemsNbt replace"
        return GenerationResult(command, ignoredItems.toList(), backportedItems.toList())
    }

    /**
     * Generates a /summon command for an armor stand with a player's equipment.
     *
     * @param player The player whose equipment is being exported.
     * @return A [GenerationResult] containing the command, or null if the player has no equipment.
     */
    fun generatePlayerArmorCommand(player: Player): GenerationResult? {
        ignoredItems.clear()
        backportedItems.clear()

        val equipment = player.equipment ?: return null
        val handItemsList = listOf(equipment.itemInMainHand, equipment.itemInOffHand)
        val armorItemsList = equipment.armorContents.toList()

        if (handItemsList.all { it.type == Material.AIR } && armorItemsList.all { it == null || it.type == Material.AIR }) {
            return null // Player has no equipment, so no command is needed.
        }

        val nbtParts = mutableListOf<String>()

        // Entities require quoted IDs
        val handItems = handItemsList
            .mapNotNull { item -> item.takeIf { it.type != Material.AIR }?.let { buildItemNbt(it, quoteId = true) } }
            .joinToString(",") { "{$it}" }
        if (handItems.isNotEmpty()) nbtParts.add("HandItems:[$handItems]")

        val armorItems = armorItemsList // Order is boots, leggings, chest, helmet
            .mapNotNull { item -> item.takeIf { it?.type != Material.AIR }?.let { buildItemNbt(it, quoteId = true) } }
            .joinToString(",") { "{$it}" }
        if (armorItems.isNotEmpty()) nbtParts.add("ArmorItems:[$armorItems]")

        val nbt = nbtParts.joinToString(",", prefix = "{", postfix = "}")
        val loc = player.location
        // Spawn the armor stand one block away from the chest
        val command = "/summon armor_stand ${loc.blockX + 1} ${loc.blockY} ${loc.blockZ} $nbt"

        return GenerationResult(command, ignoredItems.toList(), backportedItems.toList())
    }
}
