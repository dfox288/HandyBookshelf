package com.example.enchantedbookshelves.client.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;

/**
 * YACL config screen — stubbed out until YACL is ported to 26.1.
 * Config values still work via HandyBookshelvesConfig defaults.
 */
@Environment(EnvType.CLIENT)
public class HandyBookshelvesConfigScreen {

	public static Screen create(Screen parent) {
		// YACL not yet available for MC 26.1
		return null;
	}
}
