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
