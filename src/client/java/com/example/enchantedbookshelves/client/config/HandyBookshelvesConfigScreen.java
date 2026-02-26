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
