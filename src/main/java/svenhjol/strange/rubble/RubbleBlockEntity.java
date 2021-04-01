package svenhjol.strange.rubble;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

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
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        if (tag.contains(ITEMSTACK_TAG))
            this.itemStack = ItemStack.fromNbt((NbtCompound) tag.get(ITEMSTACK_TAG));

        if (tag.contains(LEVELTICKS_TAG))
            this.levelTicks = tag.getLong(LEVELTICKS_TAG);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);

        if (this.itemStack != null) {
            NbtCompound itemTag = new NbtCompound();
            this.itemStack.writeNbt(itemTag);
            tag.put(ITEMSTACK_TAG, itemTag);
        }

        tag.putLong(LEVELTICKS_TAG, levelTicks);
        return tag;
    }

    @Override
    public void fromClientTag(NbtCompound compoundTag) {
        readNbt(compoundTag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound compoundTag) {
        return writeNbt(compoundTag);
    }
}
