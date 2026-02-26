# Handy Bookshelves

A Fabric mod for Minecraft 1.21.11 that makes enchanted books visually stand out in chiseled bookshelves.

Slots containing enchanted books display the familiar enchantment shimmer, and looking at a slot shows the enchantment name — so you can tell at a glance what's stored where.

## Features

- **Enchantment Glint** — Slots containing enchanted books display the enchantment shimmer overlay.
- **Enchantment Name Tags** — Look at a bookshelf within 4 blocks and a billboard label appears above the aimed slot showing the enchantment name.
- **Configurable** — All features individually toggleable via config screen or JSON file.

## Requirements

- Minecraft Java Edition 1.21.11
- [Fabric Loader](https://fabricmc.net/use/installer/) 0.18.1+
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.139.5+

### Optional (for config screen)

- [ModMenu](https://modrinth.com/mod/modmenu) — adds a Configure button in the mod list
- [YACL](https://modrinth.com/mod/yacl) — powers the in-game config screen

Without these, all features work with sensible defaults. You can also edit `config/handybookshelves.json` manually.

## Installation

### Single Player

1. Install Fabric Loader for Minecraft 1.21.11
2. Download Fabric API and place it in your `mods/` folder
3. Download Handy Bookshelves and place it in your `mods/` folder
4. Launch the game!

### Server

The mod is required on **both the server and all connecting clients** (a mixin syncs inventory data so the client can detect enchanted books).

**Server setup:**
1. Install Fabric Loader on your server
2. Place Fabric API and Handy Bookshelves in the server's `mods/` folder
3. Start the server

**Client setup:**
1. Each player needs Fabric Loader, Fabric API, and Handy Bookshelves installed
2. Players without the mod will not see enchantment glint or name tags on bookshelves

## Building from Source

```bash
git clone https://github.com/dfox288/HandyBookshelves.git
cd HandyBookshelves

./gradlew build
# The compiled JAR will be in build/libs/
```

## Development

```bash
# Generate Minecraft sources for reference
./gradlew genSources

# Run Minecraft with the mod loaded
./gradlew runClient
```

## License

MIT License — see [LICENSE](LICENSE) for details.
