package dev.handy.mods.handybookshelf.client.render;

import dev.handy.mods.handybookshelf.config.HandyBookshelfConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Overlay-only BER for chiseled bookshelves.
 * Vanilla handles all base rendering (empty and occupied slot textures).
 * This BER adds an enchantment glint overlay on slots containing enchanted books,
 * and displays enchantment names as floating labels when the player is nearby.
 */
public class ChiseledBookshelfRenderer implements BlockEntityRenderer<ChiseledBookShelfBlockEntity, ChiseledBookshelfRenderState> {

	/**
	 * Per-slot geometry: the block-local quad in 0-1 coordinates ({@code fromX/Y..toX/Y},
	 * north-facing) and its UV window into the glint mask texture ({@code u0/v0..u1/v1}).
	 * Slots 0-2 = top row (left, mid, right), slots 3-5 = bottom row.
	 */
	private record SlotGeometry(
			float fromX, float fromY, float toX, float toY,
			float u0, float v0, float u1, float v1
	) {}

	private static final SlotGeometry[] SLOTS = {
			// slot 0: top-left (high X = player's left on north face)
			new SlotGeometry(10f/16f, 8f/16f, 16f/16f, 16f/16f,   0f/16f,  0f/16f,  6f/16f,  8f/16f),
			// slot 1: top-mid
			new SlotGeometry( 5f/16f, 8f/16f, 10f/16f, 16f/16f,   6f/16f,  0f/16f, 11f/16f,  8f/16f),
			// slot 2: top-right
			new SlotGeometry( 0f/16f, 8f/16f,  5f/16f, 16f/16f,  11f/16f,  0f/16f, 16f/16f,  8f/16f),
			// slot 3: bottom-left
			new SlotGeometry(10f/16f, 0f/16f, 16f/16f,  8f/16f,   0f/16f,  8f/16f,  6f/16f, 16f/16f),
			// slot 4: bottom-mid
			new SlotGeometry( 5f/16f, 0f/16f, 10f/16f,  8f/16f,   6f/16f,  8f/16f, 11f/16f, 16f/16f),
			// slot 5: bottom-right
			new SlotGeometry( 0f/16f, 0f/16f,  5f/16f,  8f/16f,  11f/16f,  8f/16f, 16f/16f, 16f/16f),
	};

	private static final Identifier GLINT_MASK_TEXTURE =
			Identifier.parse("handybookshelf:textures/block/chiseled_bookshelf_glint_mask.png");

	// Z offset: slightly in front of the north face (Z=0).
	// Both the opaque and glint quads render at this same Z so their depth values match.
	private static final float GLINT_Z = -0.001f;

	// Packed light value for full brightness (sky=15, block=15) — bypasses lightmap dimming.
	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;


	private final Font font;

	public ChiseledBookshelfRenderer(BlockEntityRendererProvider.Context ctx) {
		this.font = ctx.font();
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
		state.distanceToCameraSq = cameraPos.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos()));

		HandyBookshelfConfig config = HandyBookshelfConfig.get();

		// Determine which slot the crosshair is pointing at (if any)
		int aimedSlot = -1;
		double nameTagRangeSq = config.nameTagRange * (double) config.nameTagRange;
		if (config.enableNameTags && state.distanceToCameraSq <= nameTagRangeSq) {
			aimedSlot = getAimedSlot(blockEntity);
		}

		for (int slot = 0; slot < 6; slot++) {
			ItemStack stack = blockEntity.getItem(slot);
			boolean isEnchantedBook = !stack.isEmpty() && stack.is(Items.ENCHANTED_BOOK);

			state.slotGlint[slot] = isEnchantedBook && config.enableGlint;

			if (slot == aimedSlot && !stack.isEmpty()) {
				if (isEnchantedBook) {
					state.slotName[slot] = getEnchantmentLabel(stack);
				} else if (stack.is(Items.WRITTEN_BOOK) || stack.has(DataComponents.CUSTOM_NAME)) {
					state.slotName[slot] = stack.getHoverName();
				} else {
					state.slotName[slot] = null;
				}
			} else {
				state.slotName[slot] = null;
			}
		}
	}

	/**
	 * Returns the slot index the player's crosshair is pointing at, or -1 if not aiming at this bookshelf.
	 */
	private static int getAimedSlot(ChiseledBookShelfBlockEntity blockEntity) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return -1;

		BlockHitResult hit = (BlockHitResult) mc.hitResult;
		if (!hit.getBlockPos().equals(blockEntity.getBlockPos())) return -1;

		Direction facing = blockEntity.getBlockState().getValue(ChiseledBookShelfBlock.FACING);
		if (hit.getDirection() != facing) return -1;

		// Get hit position relative to the block (0-1 range)
		Vec3 hitLoc = hit.getLocation();
		double relX = hitLoc.x - hit.getBlockPos().getX();
		double relY = hitLoc.y - hit.getBlockPos().getY();
		double relZ = hitLoc.z - hit.getBlockPos().getZ();

		// Convert to face-local coordinates based on facing direction
		// For north face: u runs from high-X (left) to low-X (right), v = Y
		double u, v;
		switch (facing) {
			case NORTH -> { u = 1.0 - relX; v = relY; }
			case SOUTH -> { u = relX;        v = relY; }
			case WEST  -> { u = relZ;        v = relY; }
			case EAST  -> { u = 1.0 - relZ;  v = relY; }
			default    -> { return -1; }
		}

		// Determine column (0=left, 1=mid, 2=right) and row (0=top, 1=bottom)
		int col;
		if (u < 6.0 / 16.0)       col = 0;
		else if (u < 11.0 / 16.0) col = 1;
		else                       col = 2;

		int row = (v >= 0.5) ? 0 : 1;  // top row = high Y

		return row * 3 + col;
	}

	private static Component getEnchantmentLabel(ItemStack stack) {
		ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		if (stored.isEmpty()) {
			return stack.getHoverName();
		}

		// getFullname() applies ChatFormatting.GRAY styling — strip it via getString() so our
		// white vertex color (0xFFFFFFFF) controls the appearance instead.
		List<String> names = new ArrayList<>(stored.size());
		for (Object2IntMap.Entry<Holder<Enchantment>> entry : stored.entrySet()) {
			names.add(Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString());
		}
		return Component.literal(String.join(", ", names));
	}

	@Override
	public void submit(ChiseledBookshelfRenderState state, PoseStack poseStack,
					   SubmitNodeCollector collector, CameraRenderState cameraState) {

		boolean anyGlint = false;
		boolean anyName = false;
		for (int slot = 0; slot < 6; slot++) {
			if (state.slotGlint[slot]) anyGlint = true;
			if (state.slotName[slot] != null) anyName = true;
		}
		if (!anyGlint && !anyName) return;

		if (anyGlint) {
			submitGlint(state, poseStack, collector);
		}

		if (anyName) {
			HandyBookshelfConfig config = HandyBookshelfConfig.get();
			double nameTagRangeSq = config.nameTagRange * (double) config.nameTagRange;
			if (state.distanceToCameraSq <= nameTagRangeSq) {
				submitNameTag(state, poseStack, collector, cameraState, config);
			}
		}
	}

	private void submitGlint(ChiseledBookshelfRenderState state, PoseStack poseStack,
							 SubmitNodeCollector collector) {
		RenderType opaqueLayer = RenderTypes.entityCutout(GLINT_MASK_TEXTURE);
		RenderType glintLayer = RenderTypes.entityGlint();

		for (int slot = 0; slot < 6; slot++) {
			if (!state.slotGlint[slot]) continue;

			poseStack.pushPose();
			applyFacingRotation(poseStack, state.facing);

			final int capturedSlot = slot;

			// Pass 1: opaque textured quad (writes depth, uses mask to limit to book area)
			collector.submitCustomGeometry(poseStack, opaqueLayer,
					(pose, vertexConsumer) -> renderOpaqueQuad(pose, vertexConsumer, capturedSlot));

			// Pass 2: glint overlay (EQUAL_DEPTH_TEST matches pass 1)
			collector.submitCustomGeometry(poseStack, glintLayer,
					(pose, vertexConsumer) -> renderGlintQuad(pose, vertexConsumer, capturedSlot));

			poseStack.popPose();
		}
	}

	private void submitNameTag(ChiseledBookshelfRenderState state, PoseStack poseStack,
							   SubmitNodeCollector collector, CameraRenderState cameraState,
							   HandyBookshelfConfig config) {
		for (int slot = 0; slot < 6; slot++) {
			if (state.slotName[slot] == null) continue;

			poseStack.pushPose();

			// Calculate slot center in block-local world coordinates
			SlotGeometry geom = SLOTS[slot];
			float cx = (geom.fromX() + geom.toX()) / 2f;
			float cy = (geom.fromY() + geom.toY()) / 2f;
			Vec3 slotPos = getSlotWorldOffset(cx, cy, state.facing);
			poseStack.translate(slotPos.x, slotPos.y + 0.3, slotPos.z);

			// Billboard: face camera (same as vanilla NameTagFeatureRenderer)
			poseStack.mulPose(cameraState.orientation);

			// Text scale — vanilla name tag size, adjusted by config percentage
			float scale = 0.025F * (config.nameTagScale / 100.0f);
			poseStack.scale(scale, -scale, scale);

			// Center text horizontally
			FormattedCharSequence fcs = state.slotName[slot].getVisualOrderText();
			float textX = -this.font.width(fcs) / 2.0f;
			int textWidth = this.font.width(fcs);

			int bgColor = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25f) * 255.0f) << 24;
			int light = FULL_BRIGHT_LIGHT;

			// Background quad rendered separately (like text_display entities do),
			// in order(0) so it renders before the text.
			collector.order(1).submitCustomGeometry(poseStack,
					RenderTypes.textBackgroundSeeThrough(),
					(pose, vc) -> {
						float hw = textWidth / 2.0f + 1;
						vc.addVertex(pose, -hw, -1.0f, 0.0f).setColor(bgColor).setLight(light);
						vc.addVertex(pose, -hw, 10.0f, 0.0f).setColor(bgColor).setLight(light);
						vc.addVertex(pose, hw, 10.0f, 0.0f).setColor(bgColor).setLight(light);
						vc.addVertex(pose, hw, -1.0f, 0.0f).setColor(bgColor).setLight(light);
					});

			// Text in order(2), rendered AFTER background AND glint overlay.
			// Uses POLYGON_OFFSET (same as text_display entities) with full white.
			collector.order(2).submitText(
					poseStack, textX, 0, fcs,
					false, Font.DisplayMode.POLYGON_OFFSET,
					light, 0xFFFFFFFF, 0, 0
			);

			poseStack.popPose();
		}
	}

	/**
	 * Converts a slot center (in north-facing block-local coords) to a block-local world offset
	 * for the given facing direction. Used for name tags which must not have rotation on the PoseStack.
	 */
	private static Vec3 getSlotWorldOffset(float cx, float cy, Direction facing) {
		// In north-facing coords: cx is X, cy is Y, Z is slightly in front of face
		float offset = -0.3f;  // in front of the face
		return switch (facing) {
			case NORTH -> new Vec3(cx, cy, offset);
			case SOUTH -> new Vec3(1.0 - cx, cy, 1.0 - offset);
			case WEST  -> new Vec3(offset, cy, 1.0 - cx);
			case EAST  -> new Vec3(1.0 - offset, cy, cx);
			default    -> new Vec3(cx, cy, offset);
		};
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

	private static void renderOpaqueQuad(PoseStack.Pose pose, VertexConsumer consumer, int slot) {
		SlotGeometry geom = SLOTS[slot];
		float x0 = geom.fromX(), y0 = geom.fromY(), x1 = geom.toX(), y1 = geom.toY();
		float u0 = geom.u0(), v0 = geom.v0(), u1 = geom.u1(), v1 = geom.v1();
		float z = GLINT_Z;

		// Full entity vertex format: POSITION_COLOR_TEX_OVERLAY_LIGHTMAP_NORMAL
		// U is flipped: vanilla north face maps U left-to-right as X decreases (high X = low U)
		consumer.addVertex(pose, x0, y1, z).setColor(255, 255, 255, 255)
				.setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(FULL_BRIGHT_LIGHT).setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x0, y0, z).setColor(255, 255, 255, 255)
				.setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(FULL_BRIGHT_LIGHT).setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x1, y0, z).setColor(255, 255, 255, 255)
				.setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(FULL_BRIGHT_LIGHT).setNormal(pose, 0, 0, -1);
		consumer.addVertex(pose, x1, y1, z).setColor(255, 255, 255, 255)
				.setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(FULL_BRIGHT_LIGHT).setNormal(pose, 0, 0, -1);
	}

	private static void renderGlintQuad(PoseStack.Pose pose, VertexConsumer consumer, int slot) {
		SlotGeometry geom = SLOTS[slot];
		float x0 = geom.fromX(), y0 = geom.fromY(), x1 = geom.toX(), y1 = geom.toY();
		float z = GLINT_Z;

		// POSITION_TEX format only (matches GLINT pipeline vertex format)
		consumer.addVertex(pose, x0, y1, z).setUv(x0, y0);
		consumer.addVertex(pose, x0, y0, z).setUv(x0, y1);
		consumer.addVertex(pose, x1, y0, z).setUv(x1, y1);
		consumer.addVertex(pose, x1, y1, z).setUv(x1, y0);
	}
}
