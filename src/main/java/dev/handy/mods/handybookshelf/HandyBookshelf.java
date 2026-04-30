package dev.handy.mods.handybookshelf;

import dev.handy.mods.handybookshelf.config.HandyBookshelfConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandyBookshelf implements ModInitializer {

	public static final String MOD_ID = "handybookshelf";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		HandyBookshelfConfig.load();
		LOGGER.info("Handy Bookshelf loaded!");
	}
}
