package svenhjol.strange.module.rubble;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmSyncedBlockEntity;

public class RubbleBlockEntity extends CharmSyncedBlockEntity {
    public static final String TAG_ITEM = "item";
    public static final String TAG_LAYERTICKS = "layerticks";

    public long layerTicks;
    public ItemStack item;

    public RubbleBlockEntity(BlockPos pos, BlockState state) {
        super(Rubble.BLOCK_ENTITY, pos, state);
    }

    public void setLayerTicks(long layerTicks) {
        this.layerTicks = layerTicks;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public long getLayerTicks() {
        return layerTicks;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        Tag itemTag = tag.get(TAG_ITEM);
        if (itemTag != null) {
            item = ItemStack.of((CompoundTag)itemTag);
        }

        if (tag.contains(TAG_LAYERTICKS)) {
            layerTicks = tag.getLong(TAG_LAYERTICKS);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        if (item != null) {
            CompoundTag itemTag = new CompoundTag();
            item.save(itemTag);
            tag.put(TAG_ITEM, itemTag);
        }

        tag.putLong(TAG_LAYERTICKS, layerTicks);
    }
}
