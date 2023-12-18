package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charmony.base.CharmonySyncedBlockEntity;

public class RunestoneBlockEntity extends CharmonySyncedBlockEntity {
    public static final String LOCATION_TAG = "location";
    public static final String TARGET_TAG = "target";
    public static final String DISCOVERED_TAG = "discovered";

    public Location location;
    public BlockPos target;
    public String discovered;

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(LOCATION_TAG)) {
            this.location = Location.load(tag.getCompound(LOCATION_TAG));
        }

        if (tag.contains(TARGET_TAG)) {
            this.target = BlockPos.of(tag.getLong(TARGET_TAG));
        }

        if (tag.contains(DISCOVERED_TAG)) {
            this.discovered = tag.getString(DISCOVERED_TAG);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (location != null) {
            tag.put(LOCATION_TAG, location.save());
        }

        if (target != null) {
            tag.putLong(TARGET_TAG, target.asLong());
        }

        if (discovered != null) {
            tag.putString(DISCOVERED_TAG, discovered);
        }
    }

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Runestones.blockEntity.get(), pos, state);
    }
}
