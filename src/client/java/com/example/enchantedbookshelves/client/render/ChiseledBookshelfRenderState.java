package com.example.enchantedbookshelves.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class ChiseledBookshelfRenderState extends BlockEntityRenderState {

	/**
	 * Per-slot data: true if the slot contains an enchanted book and glint is enabled.
	 */
	public final boolean[] slotGlint = new boolean[6];

	/**
	 * Per-slot enchantment name to display as a name tag (null if no name to show).
	 */
	public final Component[] slotName = new Component[6];

	/**
	 * The facing direction of the bookshelf block.
	 */
	public Direction facing = Direction.NORTH;

	/**
	 * Squared distance from camera to block center, used for name tag rendering.
	 */
	public double distanceToCameraSq;
}
