package com.example.enchantedbookshelves.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class ChiseledBookshelfRenderState extends BlockEntityRenderState {

	/**
	 * Per-slot data: true if the slot is occupied (has an item).
	 */
	public final boolean[] slotOccupied = new boolean[6];

	/**
	 * Per-slot data: true if the slot contains an enchanted book and glint is enabled.
	 */
	public final boolean[] slotGlint = new boolean[6];

	/**
	 * The facing direction of the bookshelf block.
	 */
	public Direction facing = Direction.NORTH;

	/**
	 * Per-slot atlas-transformed UV coordinates: {u0, v0, u1, v1}.
	 * Computed from the block texture atlas sprite during state extraction.
	 */
	public float[][] atlasUVs = new float[6][4];
}
