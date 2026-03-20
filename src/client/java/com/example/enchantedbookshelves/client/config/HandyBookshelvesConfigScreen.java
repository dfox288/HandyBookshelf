package com.example.enchantedbookshelves.client.config;

import com.example.enchantedbookshelves.config.HandyBookshelvesConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
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
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handybookshelves.enableNameTags"))
								.description(OptionDescription.of(
										Component.translatable("config.handybookshelves.enableNameTags.desc")))
								.binding(true, () -> config.enableNameTags, val -> config.enableNameTags = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.handybookshelves.nameTagRange"))
								.description(OptionDescription.of(
										Component.translatable("config.handybookshelves.nameTagRange.desc")))
								.binding(4, () -> config.nameTagRange, val -> config.nameTagRange = val)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(1, 16).step(1)
										.formatValue(v -> Component.literal(v + " blocks")))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.handybookshelves.nameTagScale"))
								.description(OptionDescription.of(
										Component.translatable("config.handybookshelves.nameTagScale.desc")))
								.binding(100, () -> config.nameTagScale, val -> config.nameTagScale = val)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(50, 200).step(10)
										.formatValue(v -> Component.literal(v + "%")))
								.build())
						.build())

				.save(HandyBookshelvesConfig::save)
				.build()
				.generateScreen(parent);
	}
}
