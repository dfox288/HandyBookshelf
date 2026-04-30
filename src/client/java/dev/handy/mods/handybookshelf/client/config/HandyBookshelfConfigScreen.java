package dev.handy.mods.handybookshelf.client.config;

import dev.handy.mods.handybookshelf.config.HandyBookshelfConfig;
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
public class HandyBookshelfConfigScreen {

	public static Screen create(Screen parent) {
		HandyBookshelfConfig config = HandyBookshelfConfig.get();

		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.handybookshelf.title"))

				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.handybookshelf.category.rendering"))
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handybookshelf.enableGlint"))
								.description(OptionDescription.of(
										Component.translatable("config.handybookshelf.enableGlint.desc")))
								.binding(true, () -> config.enableGlint, val -> config.enableGlint = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.handybookshelf.enableNameTags"))
								.description(OptionDescription.of(
										Component.translatable("config.handybookshelf.enableNameTags.desc")))
								.binding(true, () -> config.enableNameTags, val -> config.enableNameTags = val)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.handybookshelf.nameTagRange"))
								.description(OptionDescription.of(
										Component.translatable("config.handybookshelf.nameTagRange.desc")))
								.binding(4, () -> config.nameTagRange, val -> config.nameTagRange = val)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(1, 16).step(1)
										.formatValue(v -> Component.literal(v + " blocks")))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.handybookshelf.nameTagScale"))
								.description(OptionDescription.of(
										Component.translatable("config.handybookshelf.nameTagScale.desc")))
								.binding(100, () -> config.nameTagScale, val -> config.nameTagScale = val)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(50, 200).step(10)
										.formatValue(v -> Component.literal(v + "%")))
								.build())
						.build())

				.save(HandyBookshelfConfig::save)
				.build()
				.generateScreen(parent);
	}
}
