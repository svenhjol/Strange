package svenhjol.strange.module.casks;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmSyncedBlockEntity;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.PotionHelper;
import svenhjol.strange.Strange;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class CaskBlockEntity extends CharmSyncedBlockEntity implements WorldlyContainer {
    public static final String PORTION_TAG = "Portions";
    public static final String EFFECTS_TAG = "Effects";
    public static final String DURATIONS_TAG = "Duration";
    public static final String AMPLIFIERS_TAG = "Amplifier";
    public static final String DILUTIONS_TAG = "Dilutions";
    public static final String NAME_TAG = "Name";

    public boolean hasBottle;
    public int portions;
    public String name;
    public Map<ResourceLocation, Integer> durations;
    public Map<ResourceLocation, Integer> amplifiers;
    public Map<ResourceLocation, Integer> dilutions;
    public List<ResourceLocation> effects;

    private static final int[] INPUT_SLOTS = new int[]{0};
    private static final int[] OUTPUT_SLOTS = new int[]{1};

    private NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

    public CaskBlockEntity(BlockPos pos, BlockState state) {
        super(Casks.BLOCK_ENTITY, pos, state);

        this.portions = 0;
        this.name = "";
        this.durations = new HashMap<>();
        this.amplifiers = new HashMap<>();
        this.dilutions = new HashMap<>();
        this.effects = new ArrayList<>();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);

        this.name = tag.getString(NAME_TAG);
        this.portions = tag.getInt(PORTION_TAG);
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
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        ContainerHelper.saveAllItems(tag, this.items);

        tag.putString(NAME_TAG, this.name);
        tag.putInt(PORTION_TAG, this.portions);

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
    }

    /**
     * Add the potion from an item into the cask.
     * True if a potion was processed, false on fail.
     */
    public boolean add(ItemStack input) {
        if (!Casks.VALID_INPUT_ITEMS.contains(input.getItem())) {
            LogHelper.debug(Strange.MOD_ID, getClass(), "The input wasn't a valid item.");
            return false;
        }

        Potion potion = PotionUtils.getPotion(input);
        List<MobEffectInstance> customEffects = PotionUtils.getCustomEffects(input);
        if (potion == Potions.EMPTY) {
            LogHelper.debug(Strange.MOD_ID, getClass(), "The input didn't have any valid potions.");
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
                    LogHelper.debug(Strange.MOD_ID, getClass(), "The input didn't have any valid effects.");
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

            LogHelper.debug(Strange.MOD_ID, getClass(), "The input was successfully added. Now " + portions + " portions.");
            return true;
        }

        LogHelper.debug(Strange.MOD_ID, getClass(), "The cask is full.");
        return false;
    }

    /**
     * Extract one portion of the cask's contents.
     * If successful this method outputs a filled glass bottle. On fail, empty itemstack.
     * Note that this method does not flush the cask if the portions reaches zero.
     */
    public ItemStack extract() {
        if (this.portions > 0) {
            // create a potion from the cask's contents
            ItemStack bottle = PotionHelper.getFilledWaterBottle(1);
            List<MobEffectInstance> effects = new ArrayList<>();

            for (ResourceLocation effectId : this.effects) {
                Registry.MOB_EFFECT.getOptional(effectId).ifPresent(statusEffect -> {
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
                    bottleName = new TextComponent(name);
                } else {
                    bottleName = new TranslatableComponent("item.strange.home_brew");
                }
                bottle.setHoverName(bottleName);
            }

            portions--;
            LogHelper.debug(Strange.MOD_ID, getClass(), "A portion was successfully extracted. Now " + portions + " portions.");
            return bottle;
        }

        LogHelper.debug(Strange.MOD_ID, getClass(), "The cask is empty, returning empty itemstack.");
        return ItemStack.EMPTY;
    }

    /**
     * Push an item into the cask and receive an output according to the item and cask state.
     *
     * If a glass bottle:
     * - if the cask is not empty, return a potion bottle.
     * - if the cask is empty, return null.
     *
     * If anything else:
     * - if it could be added to the cask, return empty itemstack.
     * - if it could not, return null.
     */
    @Nullable
    public ItemStack interact(Level level, BlockPos pos, BlockState state, ItemStack input) {
        var out = ItemStack.EMPTY;

        if (input.getItem() == Items.GLASS_BOTTLE) {
            out = extract();
            if (!out.isEmpty()) {

                // If no more portions in the cask, reset the cask data.
                if (portions <= 0) {
                    flush(level, pos, state);
                }

                input.shrink(1);
            } else {

                LogHelper.debug(Strange.MOD_ID, getClass(), "Nothing extracted, returning null.");
                return null;
            }

        } else {

            var result= add(input);
            if (result) {
                input.shrink(1);
            } else {

                LogHelper.debug(Strange.MOD_ID, getClass(), "Nothing added, returning null.");
                return null;
            }

        }

        setChanged();
        return out;
    }

    private void flush(Level level, BlockPos pos, BlockState state) {
        effects = new ArrayList<>();
        durations = new HashMap<>();
        dilutions = new HashMap<>();
        amplifiers = new HashMap<>();
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        portions = 0;

        LogHelper.debug(Strange.MOD_ID, getClass(), "No more portions, flushing the cask.");
        setChanged();
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return portions <= 0;
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
    public void setItem(int i, ItemStack itemStack) {
        if (i >= 0 && i < items.size()) {
            items.set(i, itemStack);
        }
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
        }
    }

    /**
     * Copypasta from {@link net.minecraft.world.level.block.entity.BrewingStandBlockEntity#stillValid}
     */
    @Override
    public boolean stillValid(Player player) {
        if (level == null) return false;
        if (level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return !(player.distanceToSqr((double)worldPosition.getX() + 0.5, (double)worldPosition.getY() + 0.5, (double)worldPosition.getZ() + 0.5) > 64.0);
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        if (i == 0 && getItem(0).getCount() <= 1) {
            var slot0 = itemStack.getItem();
            return slot0 == Items.GLASS_BOTTLE || slot0 == Items.POTION;
        }
        if (i == 1 && getItem(1).getCount() <= 1) {
            return itemStack.getItem() == Items.POTION;
        }
        return false;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.UP) {
            return INPUT_SLOTS;
        }
        if (direction == Direction.DOWN) {
            return OUTPUT_SLOTS;
        }
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return canPlaceItem(i, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return direction == Direction.DOWN;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (!(blockEntity instanceof CaskBlockEntity cask)) return;

        var slot0 = cask.items.get(0);
        if (slot0.isEmpty()) return;

        var out = cask.interact(level, pos, state, slot0);

        if (out == null) {
            // Wasn't able to process this item. Pass through.
            cask.setItem(1, slot0);
        } else if (out.isEmpty()) {
            // Added the contents to the cask. Return empty bottle.
            cask.setItem(1, new ItemStack(Items.GLASS_BOTTLE));
        } else {
            // Extracted contents from the cask. Return potion bottle.
            cask.setItem(1, out);
        }

        cask.setItem(0, ItemStack.EMPTY);
        cask.setChanged();
    }

}
