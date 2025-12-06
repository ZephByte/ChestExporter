package org.zephbyte.chestExporter.model

/**
 * Represents the result of a command generation.
 *
 * @param command The generated command string.
 * @param ignored A list of items that were ignored during generation.
 * @param backported A list of items that were backported to a legacy ID.
 */
data class GenerationResult(
    val command: String,
    val ignored: List<Pair<String, Int>>,
    val backported: List<Pair<String, Int>>
)
