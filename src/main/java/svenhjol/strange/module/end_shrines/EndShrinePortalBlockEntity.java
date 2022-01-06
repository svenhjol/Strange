package svenhjol.strange.module.end_shrines;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmSyncedBlockEntity;

import javax.annotation.Nullable;

public class EndShrinePortalBlockEntity extends CharmSyncedBlockEntity {
    public static final String DIMENSION_TAG = "Dimension";

    public @Nullable ResourceLocation dimension;

    public EndShrinePortalBlockEntity(BlockPos pos, BlockState state) {
        super(EndShrines.END_SHRINE_PORTAL_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        var dim = tag.getString(DIMENSION_TAG);
        dimension = dim != null ? new ResourceLocation(dim) : null;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (dimension != null) {
            tag.putString(DIMENSION_TAG, dimension.toString());
        }
    }
}
