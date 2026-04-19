package com.example.enchantedbookshelves.client;

import com.example.enchantedbookshelves.client.render.ChiseledBookshelfRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandyBookshelvesClient implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger("HandyBookshelves");

	@Override
	public void onInitializeClient() {
		LOGGER.info("[HandyBookshelves] Client initializer running — registering BER for CHISELED_BOOKSHELF");
		BlockEntityRendererRegistry.register(
				BlockEntityTypes.CHISELED_BOOKSHELF,
				ChiseledBookshelfRenderer::new
		);
		LOGGER.info("[HandyBookshelves] BER registration complete");
	}
}
