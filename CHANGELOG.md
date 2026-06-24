# Changelog

## 2.3.0-beta.1

Preview build for the **Minecraft 26.3** snapshot cycle (tested against 26.3-snapshot-1).

- Rebuilt against Fabric API **0.153.1+26.3** and YACL **3.9.5+26.3-fabric**.
- No feature changes were needed for 26.3.
- ModMenu has no 26.3 build yet, so it won't list the mod on 26.3 until it updates — the config screen is still reachable through YACL.

## 2.2.0

Updated for the **Minecraft 26.2** full release.

- Rebuilt against Fabric API **0.152.1+26.2** and Fabric Loader **0.19.3**.
- YACL updated to **3.9.4+26.2-fabric** and ModMenu to **20.0.0-beta.2**; with YACL's stable 26.2 build out, the in-game config screen is back in dev and live play.
- Now declares compatibility with `26.2` and future `26.2.x` point releases.

## 2.1.0-beta.4

- Tracks Minecraft **26.2-pre-1** (first prerelease of the 26.2 cycle); skips snapshots 6, 7, and 8 since none broke this mod's surface.
- Rebuilt against Fabric API **0.149.2+26.2**.
- YACL bumped to **3.9.3+26.2-fabric** and ModMenu to **19.0.0-alpha.1** on the compile classpath. Neither has a build that explicitly lists `26.2-pre-1` in its game_versions yet, so they remain `compileOnly` — the in-game config screen returns to dev runtime once either ships a matching build.

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
