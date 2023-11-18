package svenhjol.strange.feature.cooking_pots;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.strange.feature.cooking_pots.CookingPotContainers.EmptyContainer;
import svenhjol.strange.feature.cooking_pots.CookingPotContainers.InputContainer;

import javax.annotation.Nullable;

public class CookingPotBlockEntity extends BlockEntity implements
    WorldlyContainerHolder, Nameable {
    static final String HUNGER_TAG = "hunger";
    static final String SATURATION_TAG = "saturation";
    static final String NAME_TAG = "name";

    Component name;
    int hunger = 0;
    float saturation = 0.0f;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(CookingPots.blockEntity.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        name = Component.Serializer.fromJson(tag.getString(NAME_TAG));
        hunger = tag.getInt(HUNGER_TAG);
        saturation = tag.getFloat(SATURATION_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (this.name != null) {
            tag.putString(NAME_TAG, Component.Serializer.toJson(this.name));
        }

        tag.putInt(HUNGER_TAG, hunger);
        tag.putDouble(SATURATION_TAG, saturation);
    }

    public boolean canAddFood() {
        return isFull()
            && hasFire()
            && !hasFinishedCooking();
    }

    public boolean canAddWaterBottle() {
        return !isFull() && !hasFinishedCooking();
    }

    public boolean canAddWaterBucket() {
        return !isFull() && !hasFinishedCooking();
    }

    public boolean hasFinishedCooking() {
        return hunger >= CookingPots.getMaxHunger()
            && saturation >= CookingPots.getMaxSaturation();
    }

    public boolean hasFire() {
        if (level != null) {
            var pos = getBlockPos().below();
            return CookingPots.isValidHeatSource(level.getBlockState(pos));
        }
        return false;
    }

    public boolean isEmpty() {
        return CookingPots.isEmpty(getBlockState());
    }

    public boolean isFull() {
        return CookingPots.isFull(getBlockState());
    }

    public boolean add(ItemStack input) {
        if (!input.isEdible()) {
            return false;
        }

        if (!canAddFood()) {
            return false;
        }

        var food = input.getItem().getFoodProperties();
        if (food == null) {
            return false;
        }

        if (this.hunger <= CookingPots.getMaxHunger() || this.saturation <= CookingPots.getMaxSaturation()) {
            if (level != null) {
                var hunger = food.getNutrition();
                var saturation = food.getSaturationModifier();

                this.hunger += hunger + 1;
                this.saturation += saturation + 0.1f;

                setChanged();
                return true;
            }
        }

        return false;
    }

    public ItemStack take() {
        if (!hasFinishedCooking() || isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (level != null) {
            var bowl = getPortion();

            removePortion();

            // TODO: sound

            return bowl;
        }

        return ItemStack.EMPTY;
    }

    private void flush() {
        if (level == null) return;

        this.hunger = 0;
        this.saturation = 0;

        var pos = getBlockPos();
        var state = getBlockState();
        state = state.setValue(CookingPotBlock.PORTIONS, 0);

        level.setBlock(pos, state, 2);
        setChanged();
    }

    private void removePortion() {
        if (level == null) return;

        var pos = getBlockPos();
        var state = getBlockState();

        var portions = state.getValue(CookingPotBlock.PORTIONS) - 1;
        state = state.setValue(CookingPotBlock.PORTIONS, portions);

        if (CookingPots.isEmpty(state)) {
            flush();
        } else {
            level.setBlock(pos, state, 2);
            setChanged();
        }
    }

    private ItemStack getPortion() {
        var bowl = new ItemStack(CookingPots.mixedStewItem.get());

        if (name != null) {
            bowl.setHoverName(name);
        }

        return bowl;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        if (canAddFood()) {
            return new InputContainer(level, pos);
        }
        return new EmptyContainer();
    }

    @Override
    public Component getName() {
        if (name != null) {
            return name;
        }
        return this.getDefaultName();
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }

    Component getDefaultName() {
        return Component.translatable("container.strange.cooking_pot");
    }
}
