package svenhjol.strange.module.runic_tomes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WritingDeskBlockEntity extends BlockEntity implements Nameable {
    public static final String TAG_CUSTOM_NAME = "CustomName";
    private Component name;

    public WritingDeskBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RunicTomes.WRITING_DESK_BLOCK_ENTITY, blockPos, blockState);
    }

    /**
     * @see EnchantmentTableBlockEntity#save(CompoundTag)
     */
    @Override
    public CompoundTag save(CompoundTag nbt) {
        super.save(nbt);

        if (this.hasCustomName())
            nbt.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(this.name));

        return nbt;
    }

    /**
     * @see EnchantmentTableBlockEntity#load(CompoundTag)
     */
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains(TAG_CUSTOM_NAME, 8))
            this.name = Component.Serializer.fromJson(nbt.getString(TAG_CUSTOM_NAME));
    }

    /**
     * @see EnchantmentTableBlockEntity#getName()
     */
    @Override
    public Component getName() {
        return this.name != null ? this.name : new TranslatableComponent("container.strange.writing_desk");
    }

    /**
     * @see EnchantmentTableBlockEntity#setCustomName(Component)
     */
    public void setCustomName(@Nullable Component component) {
        this.name = component;
    }

    /**
     * @see EnchantmentTableBlockEntity#getCustomName()
     */
    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }
}
