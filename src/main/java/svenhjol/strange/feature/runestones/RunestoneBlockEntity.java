package svenhjol.strange.feature.runestones;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charmony.base.CharmonySyncedBlockEntity;

public class RunestoneBlockEntity extends CharmonySyncedBlockEntity {
    public static final String DESTINATION_TAG = "destination";
    public static final String TYPE_TAG = "type";
    public static final String TARGET_TAG = "target";
    public static final String DISCOVERED_TAG = "discovered";
    public ResourceLocation destination;
    public DestinationType type;
    public BlockPos target;
    public String discovered;

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(DESTINATION_TAG)) {
            this.destination = new ResourceLocation(tag.getString(DESTINATION_TAG));
        }

        if (tag.contains(TYPE_TAG)) {
            var type = tag.getString(TYPE_TAG);
            try {
                this.type = DestinationType.valueOf(type);
            } catch (IllegalArgumentException e) {
                this.type = null;
            }
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

        if (destination != null) {
            tag.putString(DESTINATION_TAG, destination.toString());
        }

        if (type != null) {
            tag.putString(TYPE_TAG, type.name());
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
