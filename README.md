# Chest Exporter

## ⚠️ Project Status & Disclaimer

**This is a personal project in its early stages.**

This plugin was created to help with a private server migration from a modern Minecraft version (1.21.8) to a legacy version (1.20.1). It is **not** a polished or professionally maintained solution at this time.

-   **Plugin Version**: The plugin is built to run on **Spigot 1.21.8**.
-   **Target Version**: The generated commands have only been tested for compatibility with **Minecraft 1.20.1**.
-   **Future Updates**: While more updates may come, the project is not under active, continuous development.

Please use this plugin with these limitations in mind.

---

ChestExporter is a powerful and flexible Spigot plugin designed for server administrators and players who need to transfer inventories and entities from modern Minecraft versions (1.20.5+) to legacy versions (1.16 - 1.20.4). It generates legacy-compatible `/setblock` and `/summon` commands that can be copied and pasted into older clients.

## Features

-   **Container Export**: Export any container (chests, barrels, shulker boxes, etc.) into a `/setblock` command.
-   **Double Chest Support**: Intelligently handles double chests by generating two separate commands, one for each half.
-   **Armor Stand Export**: Export any armor stand, preserving its pose, equipment, and all NBT data.
-   **Player Inventory Export**: Export a player's main inventory and armor into a chest and a separate armor stand.
-   **Configurable Item Overrides**: A simple `overrides.toml` file allows you to define custom mappings for items that don't exist in the target version. You can map new items to old ones or choose to ignore them completely.
-   **Hot Reload**: The configuration can be reloaded in-game without requiring a server restart.

## Commands

The base command for the plugin is `/chestexporter`, which can also be accessed via the aliases `/ce` and `/exportchest`.

-   `/ce export`
    -   **Description**: Looks at a container or armor stand and generates a command to recreate it.
    -   **Permission**: `chestexporter.use`

-   `/ce inv [player]`
    -   **Description**: Generates commands to export your own inventory or another player's inventory. This creates one command for a chest with the main inventory and a second command for an armor stand with the player's equipment.
    -   **Permission**: `chestexporter.inv` (for self), `chestexporter.inv.other` (for others).

-   `/ce reload`
    -   **Description**: Reloads the `overrides.toml` configuration file from disk.
    -   **Permission**: `chestexporter.reload`

## Permissions

-   `chestexporter.use`: Allows a player to use the `/ce export` command. (Default: OP)
-   `chestexporter.inv`: Allows a player to export their own inventory. (Default: OP)
-   `chestexporter.inv.other`: Allows a player to export another player's inventory. (Default: OP)
-   `chestexporter.reload`: Allows a player to reload the plugin's configuration. (Default: OP)

## Configuration

The plugin generates a configuration file at `plugins/ChestExporter/overrides.toml`. This file allows you to control how item IDs are mapped during the export process.

The format is simple:
```toml
[overrides]
"minecraft:new_item" = "minecraft:old_item"
"minecraft:another_new_item" = "some_other_plugin:custom_item"
"minecraft:item_to_ignore" = "" # An empty string means the item will be ignored
```

If an item is not found in the override map, its original ID will be used.
