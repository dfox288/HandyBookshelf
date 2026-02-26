# Enchanted Bookshelves Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** A client-side Fabric mod that renders enchantment glint on chiseled bookshelf slots containing enchanted books.

**Architecture:** Custom BlockEntityRenderer registered for `BlockEntityType.CHISELED_BOOKSHELF`. Vanilla models overridden to remove book-face quads; BER renders all book faces, adding glint for enchanted books. YACL config for toggling the effect.

**Tech Stack:** Fabric 1.21.11, Loom 1.14.10, Mojang mappings, Fabric API, YACL 3.8.1 (optional), ModMenu (optional)

---

### Task 1: Project Scaffold — Gradle & Build Files

**Files:**
- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `settings.gradle`
- Create: `.gitignore`
- Copy: `gradle/` directory from `../handyshulkers/gradle/`
- Copy: `gradlew` and `gradlew.bat` from `../handyshulkers/`

**Step 1: Initialize git repo**

```bash
cd /Users/dfox/Development/handybookshelves
git init
```

**Step 2: Copy Gradle wrapper from handyshulkers**

```bash
cp -r ../handyshulkers/gradle ./gradle
cp ../handyshulkers/gradlew ./gradlew
cp ../handyshulkers/gradlew.bat ./gradlew.bat
chmod +x gradlew
```

**Step 3: Create `gradle.properties`**

```properties
# Done to increase the memory available to gradle.
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true

# Fabric Properties
# check these on https://fabricmc.net/develop/
minecraft_version=1.21.11
loader_version=0.18.1

# Mod Properties
mod_version=1.0.0
maven_group=com.example.enchantedbookshelves
archives_base_name=handybookshelves

# Dependencies
fabric_version=0.139.5+1.21.11
yacl_version=3.8.1+1.21.11-fabric
modmenu_version=17.0.0-beta.2

# Loom
loom_version=1.14.10
```

**Step 4: Create `settings.gradle`**

```groovy
pluginManagement {
	repositories {
		maven {
			name = 'Fabric'
			url = 'https://maven.fabricmc.net/'
		}
		mavenCentral()
		gradlePluginPortal()
	}
}
```

**Step 5: Create `build.gradle`**

```groovy
plugins {
	id 'fabric-loom' version "${loom_version}"
	id 'maven-publish'
}

version = project.mod_version
group = project.maven_group

base {
	archivesName = project.archives_base_name
}

repositories {
	maven { url = "https://maven.isxander.dev/releases" }
	maven { url = "https://maven.terraformersmc.com/" }
}

loom {
	splitEnvironmentSourceSets()

	mods {
		"handybookshelves" {
			sourceSet sourceSets.main
			sourceSet sourceSets.client
		}
	}
}

dependencies {
	minecraft "com.mojang:minecraft:${project.minecraft_version}"
	mappings loom.officialMojangMappings()
	modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
	modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"

	// Config screen (YACL) and mod menu integration — both optional at runtime
	modCompileOnly("dev.isxander:yet-another-config-lib:${project.yacl_version}") {
		exclude(group: "net.fabricmc.fabric-api")
	}
	modCompileOnly "com.terraformersmc:modmenu:${project.modmenu_version}"

	// Include in dev runtime for testing the config screen
	modLocalRuntime("dev.isxander:yet-another-config-lib:${project.yacl_version}") {
		exclude(group: "net.fabricmc.fabric-api")
	}
	modLocalRuntime "com.terraformersmc:modmenu:${project.modmenu_version}"
}

processResources {
	inputs.property "version", project.version

	filesMatching("fabric.mod.json") {
		expand "version": project.version
	}
}

tasks.withType(JavaCompile).configureEach {
	it.options.release = 21
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

jar {
	from("LICENSE") {
		rename { "${it}_${project.base.archivesName.get()}" }
	}
}
```

**Step 6: Create `.gitignore`**

```
# gradle
.gradle/
build/
out/
classes/

# idea
.idea/
*.iml
*.ipr
*.iws

# vscode
.vscode/

# eclipse
.classpath
.project
.settings/
bin/

# fabric
run/
logs/
*.log

# os
.DS_Store
Thumbs.db

# other
*.jar
!gradle/wrapper/gradle-wrapper.jar

# claude
.claude/
CLAUDE.md
**/CLAUDE.md
```

**Step 7: Create directory structure**

```bash
mkdir -p src/main/java/com/example/enchantedbookshelves/config
mkdir -p src/main/resources/assets/handybookshelves/lang
mkdir -p src/client/java/com/example/enchantedbookshelves/client/render
mkdir -p src/client/java/com/example/enchantedbookshelves/client/config
mkdir -p src/client/resources/assets/minecraft/blockstates
mkdir -p src/client/resources/assets/minecraft/models/block
```

**Step 8: Build to verify scaffold**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (no source files yet, but Gradle resolves deps)

**Step 9: Commit**

```bash
git add -A
git commit -m "chore: project scaffold with Fabric 1.21.11 build files"
```

---

### Task 2: Main Entrypoint, Config & fabric.mod.json

**Files:**
- Create: `src/main/java/com/example/enchantedbookshelves/HandyBookshelves.java`
- Create: `src/main/java/com/example/enchantedbookshelves/config/HandyBookshelvesConfig.java`
- Create: `src/main/resources/fabric.mod.json`
- Create: `src/main/resources/assets/handybookshelves/lang/en_us.json`

**Step 1: Create `HandyBookshelvesConfig.java`**

```java
package com.example.enchantedbookshelves.config;

import com.example.enchantedbookshelves.HandyBookshelves;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class HandyBookshelvesConfig {

	private static HandyBookshelvesConfig INSTANCE;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handybookshelves.json");

	// -- Features --
	public boolean enableGlint = true;

	public static HandyBookshelvesConfig get() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				INSTANCE = GSON.fromJson(reader, HandyBookshelvesConfig.class);
				if (INSTANCE == null) {
					INSTANCE = new HandyBookshelvesConfig();
				}
			} catch (Exception e) {
				HandyBookshelves.LOGGER.warn("Failed to load config, using defaults", e);
				INSTANCE = new HandyBookshelvesConfig();
			}
		} else {
			INSTANCE = new HandyBookshelvesConfig();
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
		} catch (IOException e) {
			HandyBookshelves.LOGGER.warn("Failed to save config", e);
		}
	}
}
```

**Step 2: Create `HandyBookshelves.java`**

```java
package com.example.enchantedbookshelves;

import com.example.enchantedbookshelves.config.HandyBookshelvesConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandyBookshelves implements ModInitializer {

	public static final String MOD_ID = "handybookshelves";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HandyBookshelvesConfig.load();
		LOGGER.info("Handy Bookshelves loaded!");
	}
}
```

**Step 3: Create `fabric.mod.json`**

```json
{
	"schemaVersion": 1,
	"id": "handybookshelves",
	"version": "${version}",
	"name": "Handy Bookshelves",
	"description": "Enchanted books in chiseled bookshelves glow with an enchantment shimmer, so you can tell at a glance which slots hold enchanted books.",
	"authors": [
		"D.Fox"
	],
	"contact": {
		"homepage": "https://github.com/dfox288/HandyBookshelves",
		"sources": "https://github.com/dfox288/HandyBookshelves",
		"issues": "https://github.com/dfox288/HandyBookshelves/issues"
	},
	"license": "MIT",
	"icon": "assets/handybookshelves/icon.png",
	"environment": "*",
	"entrypoints": {
		"main": [
			"com.example.enchantedbookshelves.HandyBookshelves"
		],
		"client": [
			"com.example.enchantedbookshelves.client.HandyBookshelvesClient"
		],
		"modmenu": [
			"com.example.enchantedbookshelves.client.config.ModMenuIntegration"
		]
	},
	"mixins": [],
	"depends": {
		"fabricloader": ">=0.18.1",
		"minecraft": "~1.21.11",
		"java": ">=21",
		"fabric-api": "*"
	},
	"suggests": {
		"yet_another_config_lib_v3": ">=3.8.0",
		"modmenu": ">=17.0.0"
	}
}
```

**Step 4: Create `en_us.json`**

```json
{
	"config.handybookshelves.title": "Handy Bookshelves Settings",

	"config.handybookshelves.category.rendering": "Rendering",
	"config.handybookshelves.enableGlint": "Enchantment Glint",
	"config.handybookshelves.enableGlint.desc": "Show an enchantment shimmer on chiseled bookshelf slots that contain enchanted books."
}
```

**Step 5: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (client entrypoint class doesn't exist yet — that's fine, it won't fail at compile time since fabric.mod.json references are resolved at runtime)

Note: The build may warn or fail because `HandyBookshelvesClient` is referenced but doesn't exist yet. If it fails, create a minimal stub:

```java
// src/client/java/com/example/enchantedbookshelves/client/HandyBookshelvesClient.java
package com.example.enchantedbookshelves.client;

import net.fabricmc.api.ClientModInitializer;

public class HandyBookshelvesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
	}
}
```

**Step 6: Commit**

```bash
git add src/main/ src/client/java/com/example/enchantedbookshelves/client/HandyBookshelvesClient.java
git commit -m "feat: main entrypoint, config, and fabric.mod.json"
```

---

### Task 3: Vanilla Model Overrides

The BER will handle rendering book faces, so we need to override the vanilla chiseled bookshelf models to remove the book-face quads. The vanilla blockstate uses multipart format with separate models per slot state.

**Files:**
- Create: `src/client/resources/assets/minecraft/blockstates/chiseled_bookshelf.json`
- Create: 13 model files under `src/client/resources/assets/minecraft/models/block/`

**Step 1: Create the blockstate override**

The vanilla blockstate uses multipart. We keep the base shell and empty-slot faces, but replace the occupied-slot models with empty ones (no geometry) since the BER renders those.

Create `src/client/resources/assets/minecraft/blockstates/chiseled_bookshelf.json`:

```json
{
	"multipart": [
		{
			"apply": { "model": "minecraft:block/chiseled_bookshelf" }
		},
		{
			"when": { "slot_0_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_left", "uvlock": true }
		},
		{
			"when": { "slot_1_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_mid", "uvlock": true }
		},
		{
			"when": { "slot_2_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_right", "uvlock": true }
		},
		{
			"when": { "slot_3_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_left", "uvlock": true }
		},
		{
			"when": { "slot_4_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_mid", "uvlock": true }
		},
		{
			"when": { "slot_5_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_right", "uvlock": true }
		}
	]
}
```

Note: We intentionally omit the `"when": { "slot_N_occupied": "true" }` entries. Vanilla would apply occupied-slot models there, but our BER handles those. The empty-slot faces still render via the baked model so the shelf looks correct when slots are empty.

**Important:** The vanilla blockstate also applies facing rotations. We need to verify whether the vanilla multipart handles facing via the base model or via per-entry rotation. Check the actual vanilla blockstate JSON to get the exact format.

Actually, the vanilla blockstate for chiseled bookshelf does NOT use multipart rotation — it uses the `y` rotation on the `apply` object. Let me provide the correct full blockstate that accounts for facing:

```json
{
	"multipart": [
		{
			"when": { "facing": "north" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf", "y": 0, "uvlock": true }
		},
		{
			"when": { "facing": "east" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf", "y": 90, "uvlock": true }
		},
		{
			"when": { "facing": "south" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf", "y": 180, "uvlock": true }
		},
		{
			"when": { "facing": "west" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf", "y": 270, "uvlock": true }
		},
		{
			"when": { "facing": "north", "slot_0_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_left", "y": 0 }
		},
		{
			"when": { "facing": "east", "slot_0_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_left", "y": 90 }
		},
		{
			"when": { "facing": "south", "slot_0_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_left", "y": 180 }
		},
		{
			"when": { "facing": "west", "slot_0_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_left", "y": 270 }
		},
		{
			"when": { "facing": "north", "slot_1_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_mid", "y": 0 }
		},
		{
			"when": { "facing": "east", "slot_1_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_mid", "y": 90 }
		},
		{
			"when": { "facing": "south", "slot_1_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_mid", "y": 180 }
		},
		{
			"when": { "facing": "west", "slot_1_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_mid", "y": 270 }
		},
		{
			"when": { "facing": "north", "slot_2_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_right", "y": 0 }
		},
		{
			"when": { "facing": "east", "slot_2_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_right", "y": 90 }
		},
		{
			"when": { "facing": "south", "slot_2_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_right", "y": 180 }
		},
		{
			"when": { "facing": "west", "slot_2_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_top_right", "y": 270 }
		},
		{
			"when": { "facing": "north", "slot_3_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_left", "y": 0 }
		},
		{
			"when": { "facing": "east", "slot_3_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_left", "y": 90 }
		},
		{
			"when": { "facing": "south", "slot_3_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_left", "y": 180 }
		},
		{
			"when": { "facing": "west", "slot_3_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_left", "y": 270 }
		},
		{
			"when": { "facing": "north", "slot_4_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_mid", "y": 0 }
		},
		{
			"when": { "facing": "east", "slot_4_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_mid", "y": 90 }
		},
		{
			"when": { "facing": "south", "slot_4_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_mid", "y": 180 }
		},
		{
			"when": { "facing": "west", "slot_4_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_mid", "y": 270 }
		},
		{
			"when": { "facing": "north", "slot_5_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_right", "y": 0 }
		},
		{
			"when": { "facing": "east", "slot_5_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_right", "y": 90 }
		},
		{
			"when": { "facing": "south", "slot_5_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_right", "y": 180 }
		},
		{
			"when": { "facing": "west", "slot_5_occupied": "false" },
			"apply": { "model": "minecraft:block/chiseled_bookshelf_slot_bottom_right", "y": 270 }
		}
	]
}
```

The key difference from vanilla: we completely remove all the `slot_N_occupied: "true"` entries. Those occupied-face quads are now rendered by our BER.

**Step 2: No model file changes needed**

The empty-slot models (`chiseled_bookshelf_slot_top_left`, etc.) and the base shell model (`chiseled_bookshelf`) are vanilla models we reference by their original `minecraft:block/` path. We don't need to create or override them — only the blockstate file changes.

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/client/resources/
git commit -m "feat: override vanilla bookshelf blockstate to remove occupied-slot models"
```

---

### Task 4: ChiseledBookshelfRenderer (Core BER)

This is the core of the mod. The BER renders book-face quads for all occupied slots, adding enchantment glint for enchanted books.

**Files:**
- Create: `src/client/java/com/example/enchantedbookshelves/client/render/ChiseledBookshelfRenderer.java`
- Modify: `src/client/java/com/example/enchantedbookshelves/client/HandyBookshelvesClient.java`

**Step 1: Create `ChiseledBookshelfRenderer.java`**

The renderer draws a flat textured quad on the front face for each occupied slot. For enchanted books, it renders the quad a second time with a glint layer.

```java
package com.example.enchantedbookshelves.client.render;

import com.example.enchantedbookshelves.config.HandyBookshelvesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ChiseledBookshelfRenderer implements BlockEntityRenderer<ChiseledBookShelfBlockEntity> {

	private static final ResourceLocation OCCUPIED_TEXTURE =
			ResourceLocation.withDefaultNamespace("textures/block/chiseled_bookshelf_occupied.png");

	// Slot geometry in block-local coordinates (north-facing, 0-1 scale).
	// Each entry: { fromX, fromY, toX, toY } derived from vanilla model JSONs.
	// Slots 0-2 = top row (left, mid, right), slots 3-5 = bottom row.
	private static final float[][] SLOT_BOUNDS = {
			{10f/16f, 8f/16f, 16f/16f, 16f/16f},  // slot 0: top-left
			{ 5f/16f, 8f/16f, 10f/16f, 16f/16f},  // slot 1: top-mid
			{ 0f/16f, 8f/16f,  5f/16f, 16f/16f},  // slot 2: top-right
			{10f/16f, 0f/16f, 16f/16f,  8f/16f},  // slot 3: bottom-left
			{ 5f/16f, 0f/16f, 10f/16f,  8f/16f},  // slot 4: bottom-mid
			{ 0f/16f, 0f/16f,  5f/16f,  8f/16f},  // slot 5: bottom-right
	};

	// UV coordinates for each slot on the occupied texture (16x16 pixel texture).
	// Format: { u0, v0, u1, v1 } in 0-1 UV space.
	private static final float[][] SLOT_UVS = {
			{ 0f/16f,  0f/16f,  6f/16f,  8f/16f},  // slot 0
			{ 6f/16f,  0f/16f, 11f/16f,  8f/16f},  // slot 1
			{11f/16f,  0f/16f, 16f/16f,  8f/16f},  // slot 2
			{ 0f/16f,  8f/16f,  6f/16f, 16f/16f},  // slot 3
			{ 6f/16f,  8f/16f, 11f/16f, 16f/16f},  // slot 4
			{11f/16f,  8f/16f, 16f/16f, 16f/16f},  // slot 5
	};

	// Z offset for the front face (just inside the block boundary to avoid z-fighting)
	private static final float FACE_Z = 0.0001f;

	public ChiseledBookshelfRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public void render(ChiseledBookShelfBlockEntity blockEntity, float partialTick,
					   PoseStack poseStack, MultiBufferSource bufferSource,
					   int packedLight, int packedOverlay) {

		BlockState state = blockEntity.getBlockState();
		Direction facing = state.getValue(ChiseledBookShelfBlock.FACING);

		for (int slot = 0; slot < 6; slot++) {
			ItemStack stack = blockEntity.getItem(slot);
			if (stack.isEmpty()) continue;

			boolean isEnchanted = stack.is(Items.ENCHANTED_BOOK);
			boolean showGlint = isEnchanted && HandyBookshelvesConfig.get().enableGlint;

			poseStack.pushPose();

			// Rotate to match block facing — vanilla models are authored north-facing
			applyFacingRotation(poseStack, facing);

			// Render the book face quad
			renderSlotQuad(poseStack, bufferSource, slot, packedLight, packedOverlay, false);

			// Render glint overlay if enchanted
			if (showGlint) {
				renderSlotQuad(poseStack, bufferSource, slot, packedLight, packedOverlay, true);
			}

			poseStack.popPose();
		}
	}

	private void applyFacingRotation(PoseStack poseStack, Direction facing) {
		// Rotate around the block center to match facing direction
		poseStack.translate(0.5, 0.0, 0.5);
		float yRot = switch (facing) {
			case NORTH -> 0f;
			case EAST -> -90f;
			case SOUTH -> -180f;
			case WEST -> -270f;
			default -> 0f;
		};
		poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
		poseStack.translate(-0.5, 0.0, -0.5);
	}

	private void renderSlotQuad(PoseStack poseStack, MultiBufferSource bufferSource,
								int slot, int packedLight, int packedOverlay, boolean glint) {

		VertexConsumer consumer;
		if (glint) {
			consumer = bufferSource.getBuffer(RenderType.entityGlintDirect());
		} else {
			consumer = bufferSource.getBuffer(RenderType.entityCutout(OCCUPIED_TEXTURE));
		}

		PoseStack.Pose pose = poseStack.last();
		float[] bounds = SLOT_BOUNDS[slot];
		float[] uvs = SLOT_UVS[slot];

		float x0 = bounds[0], y0 = bounds[1], x1 = bounds[2], y1 = bounds[3];
		float u0 = uvs[0], v0 = uvs[1], u1 = uvs[2], v1 = uvs[3];
		float z = FACE_Z;

		// Normal pointing north (toward camera when facing north)
		// Quad vertices: bottom-left, bottom-right, top-right, top-left
		consumer.addVertex(pose, x0, y0, z).setColor(255, 255, 255, 255)
				.setUv(u0, v1).setOverlay(packedOverlay).setLight(packedLight)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x1, y0, z).setColor(255, 255, 255, 255)
				.setUv(u1, v1).setOverlay(packedOverlay).setLight(packedLight)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x1, y1, z).setColor(255, 255, 255, 255)
				.setUv(u1, v0).setOverlay(packedOverlay).setLight(packedLight)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x0, y1, z).setColor(255, 255, 255, 255)
				.setUv(u0, v0).setOverlay(packedOverlay).setLight(packedLight)
				.setNormal(pose, 0, 0, -1);
	}
}
```

**Step 2: Update `HandyBookshelvesClient.java` to register the BER**

```java
package com.example.enchantedbookshelves.client;

import com.example.enchantedbookshelves.client.render.ChiseledBookshelfRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class HandyBookshelvesClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(
				BlockEntityType.CHISELED_BOOKSHELF,
				ChiseledBookshelfRenderer::new
		);
	}
}
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/client/
git commit -m "feat: custom BER with enchantment glint for chiseled bookshelves"
```

---

### Task 5: YACL Config Screen & ModMenu Integration

**Files:**
- Create: `src/client/java/com/example/enchantedbookshelves/client/config/HandyBookshelvesConfigScreen.java`
- Create: `src/client/java/com/example/enchantedbookshelves/client/config/ModMenuIntegration.java`

**Step 1: Create `HandyBookshelvesConfigScreen.java`**

```java
package com.example.enchantedbookshelves.client.config;

import com.example.enchantedbookshelves.config.HandyBookshelvesConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class HandyBookshelvesConfigScreen {

	public static Screen create(Screen parent) {
		HandyBookshelvesConfig config = HandyBookshelvesConfig.get();

		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.handybookshelves.title"))

				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handybookshelves.category.rendering"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handybookshelves.enableGlint"))
								.description(OptionDescription.of(
										Component.translatable("config.handybookshelves.enableGlint.desc")))
								.binding(true, () -> config.enableGlint, val -> config.enableGlint = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.build())

				.save(HandyBookshelvesConfig::save)
				.build()
				.generateScreen(parent);
	}
}
```

**Step 2: Create `ModMenuIntegration.java`**

```java
package com.example.enchantedbookshelves.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3")) {
			return HandyBookshelvesConfigScreen::create;
		}
		return parent -> null;
	}
}
```

**Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/client/java/com/example/enchantedbookshelves/client/config/
git commit -m "feat: YACL config screen and ModMenu integration"
```

---

### Task 6: Create LICENSE and Placeholder Icon

**Files:**
- Create: `LICENSE`
- Create: `src/main/resources/assets/handybookshelves/icon.png`

**Step 1: Create MIT LICENSE**

```
MIT License

Copyright (c) 2026 D.Fox

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

**Step 2: Create a placeholder icon**

Copy the icon from handyshulkers as a temporary placeholder:

```bash
cp ../handyshulkers/src/main/resources/assets/handyshulkers/icon.png \
   src/main/resources/assets/handybookshelves/icon.png
```

**Step 3: Commit**

```bash
git add LICENSE src/main/resources/assets/handybookshelves/icon.png
git commit -m "chore: add MIT license and placeholder icon"
```

---

### Task 7: Full Build & In-Game Verification

**Step 1: Full clean build**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL

**Step 2: Launch dev client**

Run: `./gradlew runClient`

**Step 3: In-game testing checklist**

1. Place a chiseled bookshelf facing each direction (N, S, E, W)
2. Insert regular books — they should appear with normal book texture
3. Insert enchanted books — they should show enchantment glint shimmer
4. Mix enchanted and regular books in the same shelf — only enchanted slots shimmer
5. Leave some slots empty — empty slots show the dark empty face as vanilla
6. Place shelves adjacent to each other — no visual glitches between them
7. Check the config screen via ModMenu (if YACL is present)
8. Toggle `enableGlint` off — enchanted books should render without glint
9. Check performance with F3 — no significant FPS drop near bookshelves

**Step 4: Fix any issues found during testing**

Common issues to watch for:
- Z-fighting between BER quads and baked model — adjust `FACE_Z` offset
- Incorrect UV mapping — books look wrong, compare with vanilla
- Rotation issues — books on east/west/south-facing shelves in wrong positions
- Lighting — BER quads darker/brighter than surrounding baked faces
- Glint not visible — check RenderType and whether entityGlintDirect() works in this context

**Step 5: Final commit**

```bash
git add -A
git commit -m "chore: verified build and in-game functionality"
```

---

## Notes for the Implementing Engineer

- **Mojang mappings** are used (not Yarn). Class/method names follow Mojang conventions: `PoseStack`, `MultiBufferSource`, `getItem()`, `getValue()`, `ChiseledBookShelfBlock` (capital S).
- The **blockstate override** is the most fragile part. If the vanilla blockstate format changes between MC versions, this file needs updating. The override lives in the client source set so it only affects rendering.
- The **glint render type** (`RenderType.entityGlintDirect()`) may need adjustment. If it doesn't produce visible glint on flat quads, try `RenderType.entityGlint()`, `RenderType.glintDirect()`, or `RenderType.glint()`. The correct one depends on the rendering context (block entity vs item vs entity).
- If `ChiseledBookShelfBlockEntity.getItem()` is not accessible, you may need an accessor mixin. Check if Fabric's access wideners already expose it.
- The **slot UV coordinates** are derived from the vanilla model. Verify them against the actual `chiseled_bookshelf_occupied.png` texture by examining it in-game.
