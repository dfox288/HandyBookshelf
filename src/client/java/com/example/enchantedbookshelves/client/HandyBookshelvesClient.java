package com.example.enchantedbookshelves.client;

import com.example.enchantedbookshelves.client.render.ChiseledBookshelfRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class HandyBookshelvesClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(
				BlockEntityType.CHISELED_BOOKSHELF,
				ChiseledBookshelfRenderer::new
		);
	}
}
