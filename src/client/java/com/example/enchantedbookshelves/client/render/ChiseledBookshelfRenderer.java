package com.example.enchantedbookshelves.client.render;

import com.example.enchantedbookshelves.config.HandyBookshelvesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Overlay-only BER for chiseled bookshelves.
 * Vanilla handles all base rendering (empty and occupied slot textures).
 * This BER only adds an enchantment glint overlay on slots containing enchanted books.
 */
public class ChiseledBookshelfRenderer implements BlockEntityRenderer<ChiseledBookShelfBlockEntity, ChiseledBookshelfRenderState> {

	// Slot geometry in block-local coordinates (north-facing, 0-1 scale).
	// Each entry: { fromX, fromY, toX, toY } derived from vanilla model JSONs.
	// Slots 0-2 = top row (left, mid, right), slots 3-5 = bottom row.
	private static final float[][] SLOT_BOUNDS = {
			{10f/16f, 8f/16f, 16f/16f, 16f/16f},  // slot 0: top-left
			{ 5f/16f, 8f/16f, 10f/16f, 16f/16f},  // slot 1: top-mid
			{ 0f/16f, 8f/16f,  5f/16f, 16f/16f},  // slot 2: top-right
			{10f/16f, 0f/16f, 16f/16f,  8f/16f},  // slot 3: bottom-left
			{ 5f/16f, 0f/16f, 10f/16f,  8f/16f},  // slot 4: bottom-mid
			{ 0f/16f, 0f/16f,  5f/16f,  8f/16f},  // slot 5: bottom-right
	};

	// Z offset: slightly in front of the north face (Z=0) so the glint renders on top
	private static final float GLINT_Z = -0.001f;

	public ChiseledBookshelfRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public ChiseledBookshelfRenderState createRenderState() {
		return new ChiseledBookshelfRenderState();
	}

	@Override
	public void extractRenderState(ChiseledBookShelfBlockEntity blockEntity,
								   ChiseledBookshelfRenderState state,
								   float partialTick, Vec3 cameraPos,
								   ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);

		BlockState blockState = blockEntity.getBlockState();
		state.facing = blockState.getValue(ChiseledBookShelfBlock.FACING);

		boolean glintEnabled = HandyBookshelvesConfig.get().enableGlint;

		for (int slot = 0; slot < 6; slot++) {
			ItemStack stack = blockEntity.getItem(slot);
			state.slotGlint[slot] = !stack.isEmpty()
					&& stack.is(Items.ENCHANTED_BOOK)
					&& glintEnabled;
		}
	}

	@Override
	public void submit(ChiseledBookshelfRenderState state, PoseStack poseStack,
					   SubmitNodeCollector collector, CameraRenderState cameraState) {

		// Check if any slot needs glint — skip entirely if none
		boolean anyGlint = false;
		for (int slot = 0; slot < 6; slot++) {
			if (state.slotGlint[slot]) { anyGlint = true; break; }
		}
		if (!anyGlint) return;

		RenderType glintLayer = RenderTypes.entityGlint();

		for (int slot = 0; slot < 6; slot++) {
			if (!state.slotGlint[slot]) continue;

			poseStack.pushPose();
			applyFacingRotation(poseStack, state.facing);

			final int capturedSlot = slot;
			collector.submitCustomGeometry(poseStack, glintLayer,
					(pose, vertexConsumer) -> renderGlintQuad(pose, vertexConsumer, capturedSlot));

			poseStack.popPose();
		}
	}

	private void applyFacingRotation(PoseStack poseStack, Direction facing) {
		poseStack.translate(0.5, 0.0, 0.5);
		float yRot = switch (facing) {
			case NORTH -> 0f;
			case EAST -> -90f;
			case SOUTH -> -180f;
			case WEST -> -270f;
			default -> 0f;
		};
		poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
		poseStack.translate(-0.5, 0.0, -0.5);
	}

	private static void renderGlintQuad(PoseStack.Pose pose, VertexConsumer consumer, int slot) {
		float[] bounds = SLOT_BOUNDS[slot];
		float x0 = bounds[0], y0 = bounds[1], x1 = bounds[2], y1 = bounds[3];
		float z = GLINT_Z;

		// Counter-clockwise winding when viewed from +Z (front of shelf)
		// Glint render type uses its own texture — UVs map the glint pattern
		consumer.addVertex(pose, x0, y1, z).setColor(255, 255, 255, 255)
				.setUv(x0, y0).setLight(0xF000F0)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x0, y0, z).setColor(255, 255, 255, 255)
				.setUv(x0, y1).setLight(0xF000F0)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x1, y0, z).setColor(255, 255, 255, 255)
				.setUv(x1, y1).setLight(0xF000F0)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x1, y1, z).setColor(255, 255, 255, 255)
				.setUv(x1, y0).setLight(0xF000F0)
				.setNormal(pose, 0, 0, -1);
	}
}
