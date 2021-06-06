package svenhjol.strange.module.rubble;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RubbleBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    public static final String ITEMSTACK_TAG = "itemstack";
    public static final String LEVELTICKS_TAG = "levelticks";

    public long levelTicks;
    public ItemStack itemStack;

    public RubbleBlockEntity(BlockPos pos, BlockState state) {
        super(Rubble.BLOCK_ENTITY, pos, state);
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
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(ITEMSTACK_TAG))
            this.itemStack = ItemStack.of((CompoundTag) tag.get(ITEMSTACK_TAG));

        if (tag.contains(LEVELTICKS_TAG))
            this.levelTicks = tag.getLong(LEVELTICKS_TAG);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        super.save(tag);

        if (this.itemStack != null) {
            CompoundTag itemTag = new CompoundTag();
            this.itemStack.save(itemTag);
            tag.put(ITEMSTACK_TAG, itemTag);
        }

        tag.putLong(LEVELTICKS_TAG, levelTicks);
        return tag;
    }

    @Override
    public void fromClientTag(CompoundTag compoundTag) {
        load(compoundTag);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag compoundTag) {
        return save(compoundTag);
    }
}
