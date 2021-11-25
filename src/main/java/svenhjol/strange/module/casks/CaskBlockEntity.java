package svenhjol.strange.module.casks;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmSyncedBlockEntity;
import svenhjol.charm.helper.PotionHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CaskBlockEntity extends CharmSyncedBlockEntity {
    public static final String TAG_PORTIONS = "Portions";
    public static final String TAG_EFFECTS = "Effects";
    public static final String TAG_DURATIONS = "Duration";
    public static final String TAG_AMPLIFIERS = "Amplifier";
    public static final String TAG_DILUTIONS = "Dilutions";
    public static final String TAG_NAME = "Name";
    public static final String TAG_FERMENTATION = "Fermentation";

    public int portions = 0;
    public int fermentation = 0;
    public String name = "";
    public Map<ResourceLocation, Integer> durations = new HashMap<>();
    public Map<ResourceLocation, Integer> amplifiers = new HashMap<>();
    public Map<ResourceLocation, Integer> dilutions = new HashMap<>();
    public List<ResourceLocation> effects = new ArrayList<>();

    public CaskBlockEntity(BlockPos pos, BlockState state) {
        super(Casks.BLOCK_ENTITY, pos, state);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        this.name = nbt.getString(TAG_NAME);
        this.portions = nbt.getInt(TAG_PORTIONS);
        this.fermentation = nbt.getInt(TAG_FERMENTATION);
        this.effects = new ArrayList<>();
        this.durations = new HashMap<>();
        this.amplifiers = new HashMap<>();
        this.dilutions = new HashMap<>();

        ListTag list = nbt.getList(TAG_EFFECTS, 8);
        list.stream()
            .map(Tag::getAsString)
            .map(i -> i.replace("\"", "")) // madness
            .forEach(item -> this.effects.add(new ResourceLocation(item)));

        CompoundTag durations = nbt.getCompound(TAG_DURATIONS);
        CompoundTag amplifiers = nbt.getCompound(TAG_AMPLIFIERS);
        CompoundTag dilutions = nbt.getCompound(TAG_DILUTIONS);
        this.effects.forEach(effect -> {
            this.durations.put(effect, durations.getInt(effect.toString()));
            this.amplifiers.put(effect, amplifiers.getInt(effect.toString()));
            this.dilutions.put(effect, dilutions.getInt(effect.toString()));
        });
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putString(TAG_NAME, this.name);
        tag.putInt(TAG_PORTIONS, this.portions);
        tag.putInt(TAG_FERMENTATION, this.fermentation);

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

        tag.put(TAG_EFFECTS, effects);
        tag.put(TAG_DURATIONS, durations);
        tag.put(TAG_AMPLIFIERS, amplifiers);
        tag.put(TAG_DILUTIONS, dilutions);
    }

    public boolean add(Level level, BlockPos pos, BlockState state, ItemStack input) {
        if (input.getItem() != Items.POTION) {
            return false;
        }

        Potion potion = PotionUtils.getPotion(input);
        List<MobEffectInstance> customEffects = PotionUtils.getCustomEffects(input);
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

                List<MobEffectInstance> effects = customEffects.isEmpty() && !potion.getEffects().isEmpty() ? potion.getEffects() : customEffects;

                // strip out immediate effects and other weird things
                effects = effects.stream()
                    .filter(e -> e.getDuration() > 1)
                    .collect(Collectors.toList());

                if (effects.isEmpty()) {
                    return false;
                }

                effects.forEach(effect -> {
                    boolean changedAmplifier = false;

                    int duration = effect.getDuration();
                    int amplifier = effect.getAmplifier();

                    MobEffect type = effect.getEffect();
                    ResourceLocation effectId = Registry.MOB_EFFECT.getKey(type);
                    if (effectId == null) return;

                    if (!this.effects.contains(effectId)) {
                        this.effects.add(effectId);
                    }

                    if (this.amplifiers.containsKey(effectId)) {
                        int existingAmplifier = this.amplifiers.get(effectId);
                        changedAmplifier = amplifier != existingAmplifier;
                    }
                    this.amplifiers.put(effectId, amplifier);

                    if (!this.durations.containsKey(effectId)) {
                        this.durations.put(effectId, duration);
                    } else {
                        int existingDuration = this.durations.get(effectId);
                        if (changedAmplifier) {
                            this.durations.put(effectId, duration);
                        } else {
                            this.durations.put(effectId, existingDuration + duration);
                        }
                    }
                });
            }

            this.portions++;

            this.effects.forEach(effectId -> {
                if (!this.dilutions.containsKey(effectId)) {
                    this.dilutions.put(effectId, portions);
                } else {
                    int existingDilution = this.dilutions.get(effectId);
                    this.dilutions.put(effectId, existingDilution + 1);
                }
            });

            setChanged();

            input.shrink(1);
            return true;
        }

        return false;
    }

    @Nullable
    public ItemStack take(Level level, BlockPos pos, BlockState state, ItemStack container) {
        // might support other containers in future
        if (container.getItem() != Items.GLASS_BOTTLE) {
            return null;
        }

        if (this.portions > 0) {
            // create a potion from the cask's contents
            ItemStack bottle = PotionHelper.getFilledWaterBottle();
            List<MobEffectInstance> effects = new ArrayList<>();

            for (ResourceLocation effectId : this.effects) {
                Registry.MOB_EFFECT.getOptional(effectId).ifPresent(statusEffect -> {
                    int duration = this.durations.get(effectId);
                    int amplifier = this.amplifiers.get(effectId);
                    int dilution = this.dilutions.get(effectId);

                    effects.add(new MobEffectInstance(statusEffect, (duration / dilution) * Math.max(1, fermentation), amplifier));
                });
            }

            Component bottleName;

            if (!effects.isEmpty()) {
                PotionUtils.setCustomEffects(bottle, effects);
                if (!name.isEmpty()) {
                    bottleName = new TextComponent(name);
                } else {
                    bottleName = new TranslatableComponent("item.strange.home_brew");
                }
                bottle.setHoverName(bottleName);
            }

            container.shrink(1);

            // if no more portions in the cask, flush out the cask data
            if (--portions <= 0) {
                flush(level, pos, state);
            }

            return bottle;
        }

        return null;
    }

    public void ferment() {
        if (this.portions > 0 && this.fermentation < 5)
            this.fermentation++;
    }

    private void flush(Level level, BlockPos pos, BlockState state) {
        effects = new ArrayList<>();
        durations = new HashMap<>();
        dilutions = new HashMap<>();
        amplifiers = new HashMap<>();
        portions = 0;
        fermentation = 0;

        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            Packet<ClientGamePacketListener> updatePacket = getUpdatePacket();
            if (updatePacket != null) {
                for (ServerPlayer player : PlayerLookup.around((ServerLevel) level, worldPosition, 5)) {
                    player.connection.send(updatePacket);
                }
            }
//            BlockState state = level.getBlockState(worldPosition);
//            level.sendBlockUpdated(worldPosition, state, state, 1);
        }
    }
}
