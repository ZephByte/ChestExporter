package org.zephbyte.chestExporter.service

import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.zephbyte.chestExporter.ConfigManager
import org.zephbyte.chestExporter.model.GenerationResult
import org.zephbyte.chestExporter.util.NbtConverter
import kotlin.math.roundToInt

/**
 * A stateless service that handles the logic of generating Minecraft commands from in-game objects.
 * This class is responsible for serializing entities and containers into the appropriate
 * NBT format for legacy commands.
 *
 * @param configManager The configuration manager, used to access the item override map.
 */
class CommandGenerator(private val configManager: ConfigManager) {

    /**
     * Internal result for building item NBT, used to track outcomes.
     */
    private data class ItemNbtResult(
        val nbt: String,
        val backportedId: String? = null
    )

    /**
     * Converts a single ItemStack into its core NBT data string.
     * This is the central method for item serialization, handling item ID overrides and
     * choosing the correct NBT format (quoted vs. unquoted ID).
     *
     * @param item The ItemStack to process.
     * @param quoteId Whether the final item ID should be wrapped in quotes (for entity NBT).
     * @return An [ItemNbtResult] containing the final NBT, or null if the item should be ignored.
     */
    private fun buildItemNbt(item: ItemStack, quoteId: Boolean): ItemNbtResult? {
        val overrideMap = configManager.getOverrideMap()
        val idKey = "minecraft:${item.type.name.lowercase()}"
        var finalId = idKey
        var backported: String? = null

        if (overrideMap.containsKey(idKey)) {
            val overrideId = overrideMap[idKey]
            if (overrideId != null && overrideId.isNotBlank()) {
                backported = overrideId
                finalId = overrideId
            } else {
                // Item is explicitly ignored in the config
                return null
            }
        }

        val legacyId = finalId.removePrefix("minecraft:")
        val idString = if (quoteId) "\"$legacyId\"" else legacyId

        val tag = NbtConverter.convertMetaToLegacyNbt(item)
        val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""
        val nbt = "id:$idString,Count:${item.amount}b$tagString"

        return ItemNbtResult(nbt, backported)
    }

    /**
     * Generates a `/setblock` command for a container.
     *
     * @param container The container state to generate the command for.
     * @param inventory The specific inventory to serialize.
     * @return A [GenerationResult] containing the command and result lists.
     */
    fun generateContainerCommand(container: Container, inventory: Inventory): GenerationResult {
        val ignoredItems = mutableListOf<Pair<String, Int>>()
        val backportedItems = mutableListOf<Pair<String, Int>>()

        val block = container.block
        val blockData = container.blockData
        val blockStateStr = blockData.asString.replaceFirst("minecraft:", "")

        val itemsData = (0 until inventory.size).mapNotNull { i ->
            val item = inventory.getItem(i) ?: return@mapNotNull null
            val itemNbtResult = buildItemNbt(item, quoteId = false)
            if (itemNbtResult == null) {
                ignoredItems.add(Pair("minecraft:${item.type.name.lowercase()}", item.amount))
                return@mapNotNull null
            }
            itemNbtResult.backportedId?.let { backportedItems.add(Pair(it, item.amount)) }
            "{Slot:${i}b,${itemNbtResult.nbt}}"
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
     * Generates a `/summon` command for an armor stand.
     *
     * @param armorStand The armor stand entity to generate the command for.
     * @return A [GenerationResult] containing the command and result lists.
     */
    fun generateArmorStandCommand(armorStand: ArmorStand): GenerationResult {
        val ignoredItems = mutableListOf<Pair<String, Int>>()
        val backportedItems = mutableListOf<Pair<String, Int>>()
        val nbtParts = mutableListOf<String>()

        if (armorStand.isSmall) nbtParts.add("Small:1b")
        if (!armorStand.hasBasePlate()) nbtParts.add("NoBasePlate:1b")
        if (armorStand.hasArms()) nbtParts.add("ShowArms:1b")
        if (!armorStand.hasGravity()) nbtParts.add("NoGravity:1b")
        if (armorStand.isInvisible) nbtParts.add("Invisible:1b")
        if (armorStand.isCustomNameVisible) nbtParts.add("CustomNameVisible:1b")
        armorStand.customName?.let { nbtParts.add("CustomName:'{\"text\":\"${it.replace("'", "\\'")}\"}'") }

        val loc = armorStand.location
        nbtParts.add("Rotation:[${loc.yaw}f,${loc.pitch}f]")

        val equipment = armorStand.equipment
        if (equipment != null) {
            val handItems = listOf(equipment.itemInMainHand, equipment.itemInOffHand)
                .mapNotNull { item ->
                    item.takeIf { it.type != Material.AIR }?.let {
                        val result = buildItemNbt(it, quoteId = true)
                        if (result == null) {
                            ignoredItems.add(Pair("minecraft:${it.type.name.lowercase()}", it.amount))
                            return@mapNotNull null
                        }
                        result.backportedId?.let { backportedItems.add(Pair(it, item.amount)) }
                        "{" + result.nbt + "}"
                    }
                }
                .joinToString(",")
            if (handItems.isNotEmpty()) nbtParts.add("HandItems:[$handItems]")

            val armorItems = equipment.armorContents
                .mapNotNull { item ->
                    item.takeIf { it?.type != Material.AIR }?.let {
                        val result = buildItemNbt(it, quoteId = true)
                        if (result == null) {
                            ignoredItems.add(Pair("minecraft:${it.type.name.lowercase()}", it.amount))
                            return@mapNotNull null
                        }
                        result.backportedId?.let { backportedItems.add(Pair(it, item.amount)) }
                        "{" + result.nbt + "}"
                    }
                }
                .joinToString(",")
            if (armorItems.isNotEmpty()) nbtParts.add("ArmorItems:[$armorItems]")
        }

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

        return GenerationResult(command, ignoredItems, backportedItems)
    }

    /**
     * Generates a `/setblock` command to place a chest with the player's inventory.
     *
     * @param player The player whose inventory is being exported.
     * @return A [GenerationResult] containing the command and result lists.
     */
    fun generatePlayerInventoryCommand(player: Player): GenerationResult {
        val ignoredItems = mutableListOf<Pair<String, Int>>()
        val backportedItems = mutableListOf<Pair<String, Int>>()

        val itemsData = (0..26).mapNotNull { chestSlot ->
            val playerInvSlot = if (chestSlot < 9) chestSlot else chestSlot + 9
            val item = player.inventory.getItem(playerInvSlot) ?: return@mapNotNull null
            val itemNbtResult = buildItemNbt(item, quoteId = false)
            if (itemNbtResult == null) {
                ignoredItems.add(Pair("minecraft:${item.type.name.lowercase()}", item.amount))
                return@mapNotNull null
            }
            itemNbtResult.backportedId?.let { backportedItems.add(Pair(it, item.amount)) }
            "{Slot:${chestSlot}b,${itemNbtResult.nbt}}"
        }

        val itemsNbt = if (itemsData.isNotEmpty()) {
            itemsData.joinToString(separator = ",", prefix = "{Items:[", postfix = "]}")
        } else {
            ""
        }

        val loc = player.location
        val command = "/setblock ${loc.blockX} ${loc.blockY} ${loc.blockZ} chest$itemsNbt replace"
        return GenerationResult(command, ignoredItems, backportedItems)
    }

    /**
     * Generates a `/summon` command for an armor stand wearing a player's equipment.
     *
     * @param player The player whose equipment is being exported.
     * @return A [GenerationResult], or null if the player has no equipment.
     */
    fun generatePlayerArmorCommand(player: Player): GenerationResult? {
        val ignoredItems = mutableListOf<Pair<String, Int>>()
        val backportedItems = mutableListOf<Pair<String, Int>>()
        val equipment = player.equipment ?: return null
        val handItemsList = listOf(equipment.itemInMainHand, equipment.itemInOffHand)
        val armorItemsList = equipment.armorContents.toList()

        if (handItemsList.all { it.type == Material.AIR } && armorItemsList.all { it == null || it.type == Material.AIR }) {
            return null
        }

        val nbtParts = mutableListOf<String>()

        val handItems = handItemsList
            .mapNotNull { item ->
                item.takeIf { it.type != Material.AIR }?.let {
                    val result = buildItemNbt(it, quoteId = true)
                    if (result == null) {
                        ignoredItems.add(Pair("minecraft:${it.type.name.lowercase()}", it.amount))
                        return@mapNotNull null
                    }
                    result.backportedId?.let { backportedItems.add(Pair(it, item.amount)) }
                    "{" + result.nbt + "}"
                }
            }
            .joinToString(",")
        if (handItems.isNotEmpty()) nbtParts.add("HandItems:[$handItems]")

        val armorItems = armorItemsList
            .mapNotNull { item ->
                item.takeIf { it?.type != Material.AIR }?.let {
                    val result = buildItemNbt(it, quoteId = true)
                    if (result == null) {
                        ignoredItems.add(Pair("minecraft:${it.type.name.lowercase()}", it.amount))
                        return@mapNotNull null
                    }
                    result.backportedId?.let { backportedItems.add(Pair(it, item.amount)) }
                    "{" + result.nbt + "}"
                }
            }
            .joinToString(",")
        if (armorItems.isNotEmpty()) nbtParts.add("ArmorItems:[$armorItems]")

        val nbt = nbtParts.joinToString(",", prefix = "{", postfix = "}")
        val loc = player.location
        val command = "/summon armor_stand ${loc.blockX + 1} ${loc.blockY} ${loc.blockZ} $nbt"

        return GenerationResult(command, ignoredItems, backportedItems)
    }
}
