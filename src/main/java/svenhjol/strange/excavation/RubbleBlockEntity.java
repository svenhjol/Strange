package svenhjol.strange.excavation;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class RubbleBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    public static final String ITEMSTACK_TAG = "itemstack";
    public static final String LEVELTICKS_TAG = "levelticks";

    public long levelTicks;
    public ItemStack itemStack;

    public RubbleBlockEntity() {
        super(Excavation.BLOCK_ENTITY);
    }

    public void setLevelTicks(long levelTicks) {
        this.levelTicks = levelTicks;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public long getLevelTicks() {
        return this.levelTicks;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        if (tag.contains(ITEMSTACK_TAG))
            this.itemStack = ItemStack.fromTag((CompoundTag) tag.get(ITEMSTACK_TAG));

        if (tag.contains(LEVELTICKS_TAG))
            this.levelTicks = tag.getLong(LEVELTICKS_TAG);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);

        if (this.itemStack != null) {
            CompoundTag itemTag = new CompoundTag();
            this.itemStack.toTag(itemTag);
            tag.put(ITEMSTACK_TAG, itemTag);
        }

        tag.putLong(LEVELTICKS_TAG, levelTicks);
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag compoundTag) {
        fromTag(null, compoundTag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compoundTag) {
        return toTag(compoundTag);
    }
}
