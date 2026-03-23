# Handy Bookshelf - Fabric Mod

## Project Overview

A Fabric mod that adds enchantment glint overlays and name tags to chiseled bookshelves. Books with enchantments get a glint effect, and custom-named or written books display their name as a floating tag.

## Tech Stack

- **Minecraft**: 26.1-rc-3 (first unobfuscated version — no mappings needed)
- **Fabric Loader**: 0.18.4
- **Fabric API**: 0.143.14+26.1
- **Fabric Loom**: 1.15.5
- **Java**: 25
- **YACL**: 3.9.0+26.1-fabric (soft dependency, via Modrinth Maven `maven.modrinth:yacl`)
- **ModMenu**: 18.0.0-alpha.6 (soft dependency)

## Build

```bash
cd /Users/dfox/Development/minecraft/HandyBookshelf && ./gradlew build
cd /Users/dfox/Development/minecraft/HandyBookshelf && ./gradlew runClient
cd /Users/dfox/Development/minecraft/HandyBookshelf && ./gradlew genSources
```

Always prefix commands with `cd /path &&` so they auto-approve via permission rules.

## Project Structure

Uses `splitEnvironmentSourceSets()`:
- `src/main/` — shared code (client + server)
- `src/client/` — client-only code (rendering, config screen)

Package: `com.example.enchantedbookshelves`

## Dependencies

YACL and ModMenu are **soft dependencies** — `compileOnly`/`localRuntime` in build.gradle. The mod works without them. Config screen code checks `FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")` at runtime.

Maven repos:
- `https://maven.terraformersmc.com/` — ModMenu
- `https://api.modrinth.com/maven` — YACL (artifact: `maven.modrinth:yacl`)

## Config

YACL config screen with options for:
- Enable/disable enchantment glint
- Enable/disable name tags
- Name tag render range (1-16 blocks)
- Name tag scale (50-200%)

Config class: `HandyBookshelvesConfig` — JSON file at `config/handybookshelves.json`
