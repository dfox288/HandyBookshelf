# Enchanted Bookshelves — Design Document

**Date:** 2026-02-26
**MC Version:** 1.21.11
**Modloader:** Fabric (Loom, Mojang mappings, split source sets)
**Mod ID:** `handybookshelves`
**Group:** `com.example.enchantedbookshelves`

## Problem

Vanilla chiseled bookshelves show all books identically. Players cannot tell which slots contain enchanted books without opening the UI.

## Solution

A client-side Fabric mod that renders an enchantment glint overlay on chiseled bookshelf slots containing enchanted books.

## Approach: Full Custom BlockEntityRenderer (BER)

### Rendering

Register a `ChiseledBookshelfRenderer` as a `BlockEntityRenderer<ChiseledBookshelfBlockEntity>` via `BlockEntityRendererFactories.register()`.

The BER:
1. Iterates over all 6 slots, checks the block entity inventory
2. **Empty slot** — no rendering (vanilla shows the empty shelf face via baked model)
3. **Regular book** — renders the book quad using the vanilla book texture
4. **Enchanted book** — renders the same book quad, then re-renders with a glint render layer (`RenderType.entityGlint()` or similar)

### Slot Geometry

6 slots in a 3x2 grid. Each slot ~5.33x8 pixels on the 16x16 front face. Exact positions extracted from vanilla `chiseled_bookshelf_occupied.json` model variants. Quads are rotated based on the block's `FACING` property (N/S/E/W).

### Model Override

Bundle vanilla model overrides that remove book-face quads from the baked model (since the BER handles those). The shelf frame and empty-slot faces remain in the baked model.

Overrides live in `assets/minecraft/blockstates/` and `assets/minecraft/models/block/`.

### Config (YACL)

Following handyshulkers' pattern:
- `HandyBookshelvesConfig` — JSON-backed, MVP has one toggle: `enableGlint` (default true)
- `HandyBookshelvesConfigScreen` — YACL config screen (client source set)
- `ModMenuIntegration` — ModMenu entrypoint

YACL and ModMenu are optional runtime dependencies (compileOnly + localRuntime).

### Dependencies

Same versions as handyshulkers:
- Fabric Loader >= 0.18.1
- Fabric API 0.139.5+1.21.11
- Loom 1.14.10
- YACL 3.8.1+1.21.11-fabric (optional)
- ModMenu 17.0.0-beta.2 (optional)

## Project Structure

```
src/
├── main/
│   ├── java/com/example/enchantedbookshelves/
│   │   ├── HandyBookshelves.java
│   │   └── config/
│   │       └── HandyBookshelvesConfig.java
│   └── resources/
│       ├── fabric.mod.json
│       └── assets/handybookshelves/
│           ├── icon.png
│           └── lang/en_us.json
└── client/
    ├── java/com/example/enchantedbookshelves/client/
    │   ├── HandyBookshelvesClient.java
    │   ├── render/
    │   │   └── ChiseledBookshelfRenderer.java
    │   └── config/
    │       ├── HandyBookshelvesConfigScreen.java
    │       └── ModMenuIntegration.java
    └── resources/
        └── assets/minecraft/
            ├── blockstates/chiseled_bookshelf.json
            └── models/block/
                └── chiseled_bookshelf_*.json
```

## Out of Scope (Stretch Goals)

- Differentiating all book types (written book, book and quill)
- HUD overlay showing enchantment names
- Particle effects
- Emissive/glow textures
