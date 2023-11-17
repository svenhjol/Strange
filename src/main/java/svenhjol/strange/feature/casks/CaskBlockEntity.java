package svenhjol.strange.feature.casks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CaskBlockEntity extends BlockEntity implements
    Container, WorldlyContainer, Nameable {
    public static final String PORTIONS_TAG = "portions";
    public static final String EFFECTS_TAG = "effects";
    public static final String DURATIONS_TAG = "duration";
    public static final String AMPLIFIERS_TAG = "amplifier";
    public static final String DILUTIONS_TAG = "dilutions";
    public static final String NAME_TAG = "name";
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{1};

    int portions = 0;
    Component name;
    Map<ResourceLocation, Integer> durations = new HashMap<>();
    Map<ResourceLocation, Integer> amplifiers = new HashMap<>();
    Map<ResourceLocation, Integer> dilutions = new HashMap<>();
    List<ResourceLocation> effects = new ArrayList<>();
    NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    public CaskBlockEntity(BlockPos pos, BlockState state) {
        super(Casks.blockEntity.get(), pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.name = Component.Serializer.fromJson(tag.getString(NAME_TAG));
        this.portions = tag.getInt(PORTIONS_TAG);
        this.effects = new ArrayList<>();
        this.durations = new HashMap<>();
        this.amplifiers = new HashMap<>();
        this.dilutions = new HashMap<>();

        ListTag list = tag.getList(EFFECTS_TAG, 8);
        list.stream()
            .map(Tag::getAsString)
            .map(i -> i.replace("\"", "")) // madness
            .forEach(item -> this.effects.add(new ResourceLocation(item)));

        CompoundTag durations = tag.getCompound(DURATIONS_TAG);
        CompoundTag amplifiers = tag.getCompound(AMPLIFIERS_TAG);
        CompoundTag dilutions = tag.getCompound(DILUTIONS_TAG);
        this.effects.forEach(effect -> {
            this.durations.put(effect, durations.getInt(effect.toString()));
            this.amplifiers.put(effect, amplifiers.getInt(effect.toString()));
            this.dilutions.put(effect, dilutions.getInt(effect.toString()));
        });

        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        if (this.name != null) {
            tag.putString(NAME_TAG, Component.Serializer.toJson(this.name));
        }
        tag.putInt(PORTIONS_TAG, this.portions);

        CompoundTag durations = new CompoundTag();
        CompoundTag amplifiers = new CompoundTag();
        CompoundTag dilutions = new CompoundTag();

        ListTag effects = new ListTag();
        this.effects.forEach(effect -> {
            effects.add(StringTag.valueOf(effect.toString()));
            durations.putInt(effect.toString(), this.durations.get(effect));
            amplifiers.putInt(effect.toString(), this.amplifiers.get(effect));
            dilutions.putInt(effect.toString(), this.dilutions.get(effect));
        });

        tag.put(EFFECTS_TAG, effects);
        tag.put(DURATIONS_TAG, durations);
        tag.put(AMPLIFIERS_TAG, amplifiers);
        tag.put(DILUTIONS_TAG, dilutions);

        ContainerHelper.saveAllItems(tag, items);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CaskBlockEntity cask) {
        var input = cask.items.get(0);
        var output = cask.items.get(1);

        if (input.is(Items.GLASS_BOTTLE) && output.isEmpty()) {
            var out = cask.take();
            if (out != null) {
                cask.items.set(1, out);
                cask.setChanged();
            } else {
                cask.items.set(1, new ItemStack(Items.GLASS_BOTTLE));
            }
            input.shrink(1);
        } else if (input.is(Items.POTION) && output.isEmpty()) {
            var result = cask.add(input);
            if (result) {
                cask.items.set(1, new ItemStack(Items.GLASS_BOTTLE));
                cask.setChanged();
            } else {
                cask.items.set(1, input);
            }
            input.shrink(1);
        }
    }

    public boolean add(ItemStack input) {
        if (!input.is(Items.POTION)) {
            return false;
        }

        var potion = PotionUtils.getPotion(input);
        var customEffects = PotionUtils.getCustomEffects(input);

        if (potion == Potions.EMPTY) {
            return false;
        }

        if (portions < Casks.maxPortions) {

            // reset effects if fresh cask
            if (portions == 0) {
                this.effects = new ArrayList<>();
            }

            // potions without effects just dilute the mix
            if (potion != Potions.WATER || !customEffects.isEmpty()) {
                List<MobEffectInstance> currentEffects = customEffects.isEmpty() && !potion.getEffects().isEmpty() ? potion.getEffects() : customEffects;

                // strip out immediate effects and other weird things
                currentEffects = currentEffects.stream()
                    .filter(e -> e.getDuration() > 1)
                    .collect(Collectors.toList());

                if (currentEffects.isEmpty()) {
                    return false;
                }

                currentEffects.forEach(effect -> {
                    boolean changedAmplifier = false;

                    int duration = effect.getDuration();
                    int amplifier = effect.getAmplifier();

                    var type = effect.getEffect();
                    var effectId = BuiltInRegistries.MOB_EFFECT.getKey(type);

                    if (effectId == null)
                        return;

                    if (!effects.contains(effectId)) {
                        effects.add(effectId);
                    }

                    if (amplifiers.containsKey(effectId)) {
                        int existingAmplifier = amplifiers.get(effectId);
                        changedAmplifier = amplifier != existingAmplifier;
                    }
                    amplifiers.put(effectId, amplifier);

                    if (!durations.containsKey(effectId)) {
                        durations.put(effectId, duration);
                    } else {
                        int existingDuration = durations.get(effectId);
                        if (changedAmplifier) {
                            durations.put(effectId, duration);
                        } else {
                            durations.put(effectId, existingDuration + duration);
                        }
                    }
                });
            }

            portions++;

            effects.forEach(effectId -> {
                if (!dilutions.containsKey(effectId)) {
                    dilutions.put(effectId, portions);
                } else {
                    int existingDilution = dilutions.get(effectId);
                    dilutions.put(effectId, existingDilution + 1);
                }
            });

            if (level != null) {
                level.playSound(null, getBlockPos(), Casks.addSound.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            }

            setChanged();
            return true;
        }

        return false;
    }

    @Nullable
    public ItemStack take() {
        if (this.portions > 0) {
            var bottle = getPortion();
            removePortion();

            // Play sound
            if (level != null) {
                var pos = getBlockPos();
                if (portions > 0) {
                    level.playSound(null, pos, Casks.takeSound.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                } else {
                    level.playSound(null, pos, Casks.emptySound.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }

            return bottle;
        }

        return null;
    }

    private ItemStack getPortion() {
        // create a potion from the cask's contents
        var bottle = Casks.getFilledWaterBottle(1);
        List<MobEffectInstance> effects = new ArrayList<>();

        for (ResourceLocation effectId : this.effects) {
            BuiltInRegistries.MOB_EFFECT.getOptional(effectId).ifPresent(statusEffect -> {
                int duration = this.durations.get(effectId);
                int amplifier = this.amplifiers.get(effectId);
                int dilution = this.dilutions.get(effectId);

                effects.add(new MobEffectInstance(statusEffect, duration / dilution, amplifier));
            });
        }

        Component bottleName;

        if (!effects.isEmpty()) {
            PotionUtils.setCustomEffects(bottle, effects);
            if (name != null) {
                bottleName = name;
            } else {
                bottleName = Component.translatable("item.strange.home_brew");
            }
            bottle.setHoverName(bottleName);
        }

        return bottle;
    }

    private void removePortion() {
        // if no more portions in the cask, flush out the cask data
        if (--portions <= 0) {
            this.flush();
        }

        setChanged();
    }

    private void flush() {
        this.effects = new ArrayList<>();
        this.durations = new HashMap<>();
        this.dilutions = new HashMap<>();
        this.amplifiers = new HashMap<>();
        this.portions = 0;

        setChanged();
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

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (var item : items) {
            if (!item.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int i) {
        if (i >= 0 && i < items.size()) {
            return items.get(i);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return ContainerHelper.removeItem(items, i, j);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(items, i);
    }

    @Override
    public void setItem(int i, ItemStack stack) {
        if (i >= 0 && i < items.size()) {
            items.set(i, stack);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        return new int[]{};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return items.get(slot).isEmpty() && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return !items.get(slot).isEmpty() && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(Items.GLASS_BOTTLE) || stack.is(Items.POTION);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
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
        return Component.translatable("container.strange.cask");
    }
}
