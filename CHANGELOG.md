# Changelog

## 2.1.0-beta.3

### Breaking
- **Mod ID renamed from `handybookshelves` to `handybookshelf`** to match the rest of the Handy series convention (singular). Existing config at `config/handybookshelves.json` is migrated automatically on first launch — no settings lost.
- Internal package moved from `com.example.enchantedbookshelves` to `dev.handy.mods.handybookshelf`. No user-facing impact unless another mod was depending on internal classes.

## 2.1.0-beta.1

- Preview build for Minecraft **26.2 snapshots** (tested against 26.2-snapshot-3)
- Rebuilt against Fabric API 0.146.1+26.2
- Adapted to the 26.2 registry split: `BlockEntityType.CHISELED_BOOKSHELF` moved to the new `BlockEntityTypes` class, so the BER registration imports `BlockEntityTypes` instead

## 2.0.2

- Update to Minecraft 26.1.2 compatibility
- Update Fabric Loader to 0.19.2, Fabric API to 0.146.1

## 2.0.1

- Update to Minecraft 26.1.1 compatibility
- Update Fabric Loader to 0.18.6, Fabric API to 0.145.3, YACL to 3.9.2

## 2.0.0

- Port to Minecraft 26.1 (Java 25, unobfuscated)
- Restore YACL config screen integration

## 1.2.0-beta.1

- Port to Minecraft 26.1-rc-1 (Java 25, unobfuscated)
- Restore YACL config screen integration
- Add CI/CD pipeline with automated Modrinth publishing
