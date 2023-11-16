package svenhjol.strange.feature.casks;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CaskBlockEntity extends BlockEntity {
    public static final String PORTIONS_TAG = "portions";
    public static final String EFFECTS_TAG = "effects";
    public static final String DURATIONS_TAG = "duration";
    public static final String AMPLIFIERS_TAG = "amplifier";
    public static final String DILUTIONS_TAG = "dilutions";
    public static final String NAME_TAG = "name";

    int portions = 0;
    String name = "";
    Map<ResourceLocation, Integer> durations = new HashMap<>();
    Map<ResourceLocation, Integer> amplifiers = new HashMap<>();
    Map<ResourceLocation, Integer> dilutions = new HashMap<>();
    List<ResourceLocation> effects = new ArrayList<>();

    public CaskBlockEntity(BlockPos pos, BlockState state) {
        super(Casks.blockEntity.get(), pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        this.name = nbt.getString(NAME_TAG);
        this.portions = nbt.getInt(PORTIONS_TAG);
        this.effects = new ArrayList<>();
        this.durations = new HashMap<>();
        this.amplifiers = new HashMap<>();
        this.dilutions = new HashMap<>();

        ListTag list = nbt.getList(EFFECTS_TAG, 8);
        list.stream()
            .map(Tag::getAsString)
            .map(i -> i.replace("\"", "")) // madness
            .forEach(item -> this.effects.add(new ResourceLocation(item)));

        CompoundTag durations = nbt.getCompound(DURATIONS_TAG);
        CompoundTag amplifiers = nbt.getCompound(AMPLIFIERS_TAG);
        CompoundTag dilutions = nbt.getCompound(DILUTIONS_TAG);
        this.effects.forEach(effect -> {
            this.durations.put(effect, durations.getInt(effect.toString()));
            this.amplifiers.put(effect, amplifiers.getInt(effect.toString()));
            this.dilutions.put(effect, dilutions.getInt(effect.toString()));
        });
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putString(NAME_TAG, this.name);
        nbt.putInt(PORTIONS_TAG, this.portions);

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

        nbt.put(EFFECTS_TAG, effects);
        nbt.put(DURATIONS_TAG, durations);
        nbt.put(AMPLIFIERS_TAG, amplifiers);
        nbt.put(DILUTIONS_TAG, dilutions);
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

            setChanged();

            input.shrink(1);
            return true;
        }

        return false;
    }

    @Nullable
    public ItemStack take(ItemStack container) {
        // might support other containers in future
        if (container.getItem() != Items.GLASS_BOTTLE) {
            return null;
        }

        if (this.portions > 0) {
            // create a potion from the cask's contents
            ItemStack bottle = Casks.getFilledWaterBottle(1);
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
                if (!name.isEmpty()) {
                    bottleName = Component.literal(name);
                } else {
                    bottleName = Component.translatable("item.strange.home_brew");
                }
                bottle.setHoverName(bottleName);
            }

            container.shrink(1);

            // if no more portions in the cask, flush out the cask data
            if (--portions <= 0) {
                this.flush();
            }

            setChanged();
            return bottle;
        }

        return null;
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
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private void flush() {
        this.effects = new ArrayList<>();
        this.durations = new HashMap<>();
        this.dilutions = new HashMap<>();
        this.amplifiers = new HashMap<>();
        this.portions = 0;

        setChanged();
    }
}
