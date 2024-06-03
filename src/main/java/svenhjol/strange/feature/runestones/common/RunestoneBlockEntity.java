package svenhjol.strange.feature.runestones.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.charmony.Resolve;
import svenhjol.charm.charmony.common.block.entity.CharmBlockEntity;
import svenhjol.strange.api.impl.RunestoneLocation;
import svenhjol.strange.feature.runestones.Runestones;

public class RunestoneBlockEntity extends CharmBlockEntity<Runestones> {
    public static final Runestones RUNESTONES = Resolve.feature(Runestones.class);
    public static final String LOCATION_TAG = "location";

    public RunestoneLocation location;

    public RunestoneBlockEntity(BlockPos pos, BlockState state) {
        super(Resolve.feature(Runestones.class).registers.blockEntity.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains(LOCATION_TAG)) {
            this.location = RunestoneLocation.load(tag.getCompound(LOCATION_TAG));
        }

        // TODO: other tag data
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        if (location != null) {
            tag.put(LOCATION_TAG, location.save());
        }

        // TODO: other tag data
    }

    @Override
    public Class<Runestones> typeForFeature() {
        return Runestones.class;
    }

    public boolean isActivated() {
        var state = getBlockState();
        return state.getValue(RunestoneBlock.ACTIVATED);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RunestoneBlockEntity runestone) {
        if (level.getGameTime() % 120 == 0) {
            if (runestone.location != null) {
                RUNESTONES.log().debug(runestone.location.toString());
            }
        }
    }
}
