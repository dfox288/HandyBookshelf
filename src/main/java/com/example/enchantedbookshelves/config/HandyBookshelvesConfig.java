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
	public boolean enableNameTags = true;
	public int nameTagRange = 4;        // blocks (1-16)
	public int nameTagScale = 100;      // percentage (50-200)

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
