package svenhjol.strange.module.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmSyncedBlockEntity;

public class DataBlockEntity extends CharmSyncedBlockEntity {
    public static final String METADATA_TAG = "metadata";

    private String metadata = "";
    private ItemStack cachedItem = null;

    public DataBlockEntity(BlockPos pos, BlockState state) {
        super(Structures.DATA_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(METADATA_TAG, metadata != null ? metadata : "");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        metadata = tag.getString(METADATA_TAG);
    }

    public ItemStack getItem() {
        if (cachedItem == null) {
            for (String s : Structures.DECORATION_ITEM_MAP.keySet()) {
                if (metadata.startsWith(s)) {
                    cachedItem = new ItemStack(Structures.DECORATION_ITEM_MAP.get(s));
                }
            }

            if (cachedItem == null) {
                cachedItem = new ItemStack(Blocks.STRUCTURE_BLOCK);
            }
        }

        return cachedItem;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public void setChanged() {
        cachedItem = null;
        super.setChanged();
    }
}
