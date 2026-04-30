package dev.handy.mods.handybookshelf.client;

import dev.handy.mods.handybookshelf.client.render.ChiseledBookshelfRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public class HandyBookshelfClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.register(
				BlockEntityTypes.CHISELED_BOOKSHELF,
				ChiseledBookshelfRenderer::new
		);
	}
}
