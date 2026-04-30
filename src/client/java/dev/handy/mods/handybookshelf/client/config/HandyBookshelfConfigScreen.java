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

import java.util.function.Consumer;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class HandyBookshelfConfigScreen {

	private static final String I18N_PREFIX = "config.handybookshelf.";

	public static Screen create(Screen parent) {
		HandyBookshelfConfig config = HandyBookshelfConfig.get();

		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable(I18N_PREFIX + "title"))

				.category(ConfigCategory.createBuilder()
						.name(Component.translatable(I18N_PREFIX + "category.rendering"))
						.option(booleanOption("enableGlint",
								() -> config.enableGlint, val -> config.enableGlint = val))
						.option(booleanOption("enableNameTags",
								() -> config.enableNameTags, val -> config.enableNameTags = val))
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable(I18N_PREFIX + "nameTagRange"))
								.description(OptionDescription.of(
										Component.translatable(I18N_PREFIX + "nameTagRange.desc")))
								.binding(5, () -> config.nameTagRange, val -> config.nameTagRange = val)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										// Capped at 5 to match vanilla player reach — the underlying
										// aim check uses Minecraft.hitResult, which doesn't extend
										// past reach, so values above 5 had no visible effect.
										.range(1, 5).step(1)
										.formatValue(v -> Component.literal(v + " blocks")))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable(I18N_PREFIX + "nameTagScale"))
								.description(OptionDescription.of(
										Component.translatable(I18N_PREFIX + "nameTagScale.desc")))
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

	/**
	 * Builds a tick-box {@link Option} with name + description sourced from the standard
	 * translation keys ({@code config.handybookshelf.<key>} and {@code .desc}).
	 */
	private static Option<Boolean> booleanOption(String key,
												 Supplier<Boolean> getter,
												 Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
				.name(Component.translatable(I18N_PREFIX + key))
				.description(OptionDescription.of(
						Component.translatable(I18N_PREFIX + key + ".desc")))
				.binding(true, getter::get, setter::accept)
				.controller(TickBoxControllerBuilder::create)
				.build();
	}
}
