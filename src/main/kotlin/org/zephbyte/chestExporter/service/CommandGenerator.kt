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
 * A stateless service for generating Minecraft commands.
 */
class CommandGenerator(private val configManager: ConfigManager) {

    /**
     * Internal result for building item NBT.
     */
    private data class ItemNbtResult(
        val nbt: String,
        val backportedId: String? = null,
        val ignoredId: String? = null
    )

    private fun buildItemNbt(item: ItemStack, quoteId: Boolean): ItemNbtResult? {
        val overrideMap = configManager.getOverrideMap()
        val idKey = "minecraft:${item.type.name.lowercase()}"
        var finalId = idKey
        var backported: String? = null
        var ignored: String? = null

        if (overrideMap.containsKey(idKey)) {
            val overrideId = overrideMap[idKey]
            if (overrideId != null && overrideId.isNotBlank()) {
                backported = overrideId
                finalId = overrideId
            } else {
                ignored = idKey
                return null
            }
        }

        val legacyId = finalId.removePrefix("minecraft:")
        val idString = if (quoteId) "\"$legacyId\"" else legacyId

        val tag = NbtConverter.convertMetaToLegacyNbt(item)
        val tagString = if (tag.isNotEmpty()) ",tag:{$tag}" else ""
        val nbt = "id:$idString,Count:${item.amount}b$tagString"

        return ItemNbtResult(nbt, backported, ignored)
    }

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

    fun generateArmorStandCommand(armorStand: ArmorStand): GenerationResult {
        val ignoredItems = mutableListOf<Pair<String, Int>>()
        val backportedItems = mutableListOf<Pair<String, Int>>()
        val nbtParts = mutableListOf<String>()

        if (armorStand.isSmall) nbtParts.add("Small:1b")
        //if (!armorStand.isBasePlateVisible) nbtParts.add("NoBasePlate:1b")
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
