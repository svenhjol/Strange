package svenhjol.strange.module.rubble;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RubbleBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
    public static final String ITEMSTACK_NBT = "ItemStack";
    public static final String LEVELTICKS_NBT = "LevelTicks";

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
    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(ITEMSTACK_NBT))
            this.itemStack = ItemStack.of((CompoundTag) nbt.get(ITEMSTACK_NBT));

        if (nbt.contains(LEVELTICKS_NBT))
            this.levelTicks = nbt.getLong(LEVELTICKS_NBT);
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        super.save(nbt);

        if (this.itemStack != null) {
            CompoundTag itemTag = new CompoundTag();
            this.itemStack.save(itemTag);
            nbt.put(ITEMSTACK_NBT, itemTag);
        }

        nbt.putLong(LEVELTICKS_NBT, levelTicks);
        return nbt;
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
