package svenhjol.strange.feature.cooking_pots;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class CookingPotBlockEntity extends BlockEntity {
    static final String HUNGER_TAG = "hunger";
    static final String SATURATION_TAG = "saturation";

    int hunger = 0;
    float saturation = 0.0f;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(CookingPots.blockEntity.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        hunger = tag.getInt(HUNGER_TAG);
        saturation = tag.getFloat(SATURATION_TAG);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt(HUNGER_TAG, hunger);
        tag.putDouble(SATURATION_TAG, saturation);
    }

    public boolean canAddFood() {
        return isFull()
            && hasFire()
            && !hasFinishedCooking();
    }

    public boolean canAddWater() {
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
                var pos = getBlockPos();
                var state = getBlockState();
                var random = level.getRandom();
                var hunger = food.getNutrition();
                var saturation = food.getSaturationModifier();

                this.hunger += hunger + random.nextInt(1);
                this.saturation += saturation + (random.nextFloat() * 0.1f);

                if (hasFinishedCooking()) {
                    state = state.setValue(CookingPotBlock.COOKING_STATUS, CookingStatus.COOKED);
                } else {
                    state = state.setValue(CookingPotBlock.COOKING_STATUS, CookingStatus.IN_PROGRESS);
                }

                level.setBlock(pos, state, 3);

                setChanged();

                // Let nearby players know an item was added to the pot
                if (!level.isClientSide) {
                    level.playSound(null, pos, CookingPots.addSound.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
                    CookingPotsNetwork.AddedToCookingPot.send(level, pos);
                }

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

            if (!level.isClientSide) {
                level.playSound(null, getBlockPos(), CookingPots.takeSound.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
            }

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
        state = state
            .setValue(CookingPotBlock.PORTIONS, 0)
            .setValue(CookingPotBlock.COOKING_STATUS, CookingStatus.NONE);

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
        return bowl;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
