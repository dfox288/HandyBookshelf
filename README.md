# Handy Bookshelves

A Fabric mod for Minecraft 1.21.11 that makes enchanted books visually stand out in chiseled bookshelves.

## Features

- **Enchantment Glint**: Slots containing enchanted books display the familiar enchantment shimmer overlay, so you can tell at a glance which slots hold enchanted books
- **Enchantment Name Tags**: Look at a bookshelf within 4 blocks and a billboard label appears above the aimed slot showing the enchantment name
- **Configurable**: YACL config screen with Mod Menu integration

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.18.1+
- Fabric API
- Java 21+
- **Must be installed on both client and server** (a mixin syncs inventory data to the client)

### Optional

- [YACL](https://modrinth.com/mod/yacl) 3.8.0+ — for the config screen
- [Mod Menu](https://modrinth.com/mod/modmenu) 17.0.0+ — for config screen access from the mod list

## Building

```
./gradlew build
```

Output jar: `build/libs/handybookshelves-1.0.0.jar`

## License

MIT
