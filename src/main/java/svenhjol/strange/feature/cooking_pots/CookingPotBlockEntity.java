package svenhjol.strange.feature.cooking_pots;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CookingPotBlockEntity extends BlockEntity {
    static final String HUNGER_TAG = "hunger";
    static final String SATURATION_TAG = "saturation";
    static final String NAME_TAG = "name";

    Component name;
    int hunger = 0;
    double saturation = 0.0;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(CookingPots.blockEntity.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        name = Component.Serializer.fromJson(tag.getString(NAME_TAG));
        hunger = tag.getInt(HUNGER_TAG);
        saturation = tag.getDouble(SATURATION_TAG);
    }
}
