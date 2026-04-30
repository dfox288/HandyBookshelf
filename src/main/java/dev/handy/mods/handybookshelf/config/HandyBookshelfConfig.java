package dev.handy.mods.handybookshelf.config;

import dev.handy.mods.handybookshelf.HandyBookshelf;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class HandyBookshelfConfig {

	private static HandyBookshelfConfig INSTANCE;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handybookshelf.json");
	// Legacy path from before the v2.1 mod-id rename (handybookshelves → handybookshelf).
	// Read once on first load so user settings carry over; safe to remove after a few releases.
	private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir().resolve("handybookshelves.json");

	// -- Features --
	public boolean enableGlint = true;
	public boolean enableNameTags = true;
	public int nameTagRange = 4;        // blocks (1-16)
	public int nameTagScale = 100;      // percentage (50-200)

	public static HandyBookshelfConfig get() {
		if (INSTANCE == null) {
			load();
		}
		return INSTANCE;
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH) && Files.exists(LEGACY_CONFIG_PATH)) {
			try {
				Files.copy(LEGACY_CONFIG_PATH, CONFIG_PATH);
				HandyBookshelf.LOGGER.info("Migrated config from {} to {}",
						LEGACY_CONFIG_PATH.getFileName(), CONFIG_PATH.getFileName());
			} catch (IOException e) {
				HandyBookshelf.LOGGER.warn("Failed to migrate legacy config from {}",
						LEGACY_CONFIG_PATH.getFileName(), e);
			}
		}

		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				INSTANCE = GSON.fromJson(reader, HandyBookshelfConfig.class);
				if (INSTANCE == null) {
					INSTANCE = new HandyBookshelfConfig();
				}
			} catch (JsonSyntaxException | IOException e) {
				HandyBookshelf.LOGGER.warn("Failed to load config, using defaults", e);
				INSTANCE = new HandyBookshelfConfig();
			}
		} else {
			INSTANCE = new HandyBookshelfConfig();
			save();
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
		} catch (IOException e) {
			HandyBookshelf.LOGGER.warn("Failed to save config", e);
		}
	}
}
