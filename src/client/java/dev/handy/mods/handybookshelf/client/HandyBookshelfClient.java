package dev.handy.mods.handybookshelf.client;

import dev.handy.mods.handybookshelf.client.render.ChiseledBookshelfRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandyBookshelfClient implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger("HandyBookshelf");

	@Override
	public void onInitializeClient() {
		LOGGER.info("[HandyBookshelf] Client initializer running — registering BER for CHISELED_BOOKSHELF");
		BlockEntityRendererRegistry.register(
				BlockEntityTypes.CHISELED_BOOKSHELF,
				ChiseledBookshelfRenderer::new
		);
		LOGGER.info("[HandyBookshelf] BER registration complete");
	}
}
