package com.example.enchantedbookshelves.client.render;

import com.example.enchantedbookshelves.config.HandyBookshelvesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ChiseledBookshelfRenderer implements BlockEntityRenderer<ChiseledBookShelfBlockEntity, ChiseledBookshelfRenderState> {

	private static final Identifier OCCUPIED_SPRITE_ID =
			Identifier.withDefaultNamespace("block/chiseled_bookshelf_occupied");

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

	// UV coordinates for each slot on the occupied texture (16x16 pixel texture).
	// Format: { u0, v0, u1, v1 } in 0-1 UV space.
	private static final float[][] SLOT_UVS = {
			{ 0f/16f,  0f/16f,  6f/16f,  8f/16f},  // slot 0
			{ 6f/16f,  0f/16f, 11f/16f,  8f/16f},  // slot 1
			{11f/16f,  0f/16f, 16f/16f,  8f/16f},  // slot 2
			{ 0f/16f,  8f/16f,  6f/16f, 16f/16f},  // slot 3
			{ 6f/16f,  8f/16f, 11f/16f, 16f/16f},  // slot 4
			{11f/16f,  8f/16f, 16f/16f, 16f/16f},  // slot 5
	};

	// Z offset for the front face (just inside the block boundary to avoid z-fighting)
	private static final float FACE_Z = 0.0001f;

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
			state.slotOccupied[slot] = !stack.isEmpty();
			state.slotGlint[slot] = !stack.isEmpty()
					&& stack.is(Items.ENCHANTED_BOOK)
					&& glintEnabled;
		}

		// Look up the sprite on the block texture atlas and transform raw UVs
		TextureAtlasSprite sprite = Minecraft.getInstance()
				.getAtlasManager()
				.getAtlasOrThrow(AtlasIds.BLOCKS)
				.getSprite(OCCUPIED_SPRITE_ID);

		for (int slot = 0; slot < 6; slot++) {
			float[] raw = SLOT_UVS[slot];
			// raw values are in 0-1 space; multiply by 16 for pixel coordinates
			state.atlasUVs[slot][0] = sprite.getU(raw[0] * 16f);
			state.atlasUVs[slot][1] = sprite.getV(raw[1] * 16f);
			state.atlasUVs[slot][2] = sprite.getU(raw[2] * 16f);
			state.atlasUVs[slot][3] = sprite.getV(raw[3] * 16f);
		}
	}

	@Override
	public void submit(ChiseledBookshelfRenderState state, PoseStack poseStack,
					   SubmitNodeCollector collector, CameraRenderState cameraState) {

		RenderType textureLayer = Sheets.cutoutBlockSheet();
		RenderType glintLayer = RenderTypes.entityGlint();

		for (int slot = 0; slot < 6; slot++) {
			if (!state.slotOccupied[slot]) continue;

			poseStack.pushPose();

			// Rotate to match block facing - vanilla models are authored north-facing
			applyFacingRotation(poseStack, state.facing);

			// Render the book face quad using atlas-transformed UVs
			final int capturedSlot = slot;
			final float[] slotAtlasUVs = state.atlasUVs[slot];
			collector.submitCustomGeometry(poseStack, textureLayer,
					(pose, vertexConsumer) -> renderSlotQuad(pose, vertexConsumer, capturedSlot, state.lightCoords, slotAtlasUVs));

			// Render glint overlay if enchanted
			if (state.slotGlint[slot]) {
				collector.submitCustomGeometry(poseStack, glintLayer,
						(pose, vertexConsumer) -> renderSlotQuad(pose, vertexConsumer, capturedSlot, state.lightCoords, slotAtlasUVs));
			}

			poseStack.popPose();
		}
	}

	private void applyFacingRotation(PoseStack poseStack, Direction facing) {
		// Rotate around the block center to match facing direction
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

	private static void renderSlotQuad(PoseStack.Pose pose, VertexConsumer consumer,
										int slot, int packedLight, float[] atlasUVs) {

		float[] bounds = SLOT_BOUNDS[slot];

		float x0 = bounds[0], y0 = bounds[1], x1 = bounds[2], y1 = bounds[3];
		float u0 = atlasUVs[0], v0 = atlasUVs[1], u1 = atlasUVs[2], v1 = atlasUVs[3];
		float z = FACE_Z;

		// Normal pointing north (toward camera when facing north)
		// Quad vertices: bottom-left, bottom-right, top-right, top-left
		consumer.addVertex(pose, x0, y0, z).setColor(255, 255, 255, 255)
				.setUv(u0, v1).setLight(packedLight)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x1, y0, z).setColor(255, 255, 255, 255)
				.setUv(u1, v1).setLight(packedLight)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x1, y1, z).setColor(255, 255, 255, 255)
				.setUv(u1, v0).setLight(packedLight)
				.setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x0, y1, z).setColor(255, 255, 255, 255)
				.setUv(u0, v0).setLight(packedLight)
				.setNormal(pose, 0, 0, -1);
	}
}
