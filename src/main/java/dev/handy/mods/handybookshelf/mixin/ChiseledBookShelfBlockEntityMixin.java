package dev.handy.mods.handybookshelf.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin to sync ChiseledBookShelfBlockEntity inventory data to the client.
 * Vanilla only syncs blockstate (slot_N_occupied booleans), not the actual items.
 * We need the item data client-side to detect enchanted books for glint rendering.
 */
@Mixin(ChiseledBookShelfBlockEntity.class)
public abstract class ChiseledBookShelfBlockEntityMixin extends BlockEntity {

	private ChiseledBookShelfBlockEntityMixin() {
		super(null, null, null);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create((BlockEntity) (Object) this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return this.saveCustomOnly(provider);
	}
}
