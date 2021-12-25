package svenhjol.strange.module.cooking_pots;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import svenhjol.charm.block.CharmSyncedBlockEntity;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeSounds;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("unused")
public class CookingPotBlockEntity extends CharmSyncedBlockEntity {
    public static final String TAG_HUNGER = "Hunger";
    public static final String TAG_SATURATION = "Saturation";
    public static final String TAG_CONTENTS = "Contents";
    public static final String TAG_EFFECTS = "Effects";
    public static final String TAG_NAME = "Name";

    public String name = "";

    public List<ResourceLocation> contents = new ArrayList<>();
    public List<MobEffectInstance> effects = new ArrayList<>();
    public int hunger;
    public float saturation;

    public float displayTicks = 0.0F;
    public int displayIndex = 0;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(CookingPots.BLOCK_ENTITY, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.contents = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.name = tag.getString(TAG_NAME);
        this.hunger = tag.getInt(TAG_HUNGER);
        this.saturation = tag.getFloat(TAG_SATURATION);

        ListTag effects = tag.getList(TAG_EFFECTS, 10);
        effects.stream()
            .map(t -> (CompoundTag)t)
            .forEach(c -> this.effects.add(MobEffectInstance.load(c)));

        ListTag contents = tag.getList(TAG_CONTENTS, 8);
        contents.stream()
            .map(Tag::getAsString)
            .map(i -> i.replace("\"", "")) // madness
            .forEach(item -> this.contents.add(new ResourceLocation(item)));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putString(TAG_NAME, this.name);

        ListTag contents = new ListTag();
        this.contents.forEach(food -> contents.add(StringTag.valueOf(food.toString())));

        ListTag effects = new ListTag();
        this.effects.forEach(effect -> {
            CompoundTag t = new CompoundTag();
            effects.add(effect.save(t));
        });

        tag.put(TAG_CONTENTS, contents);
        tag.put(TAG_EFFECTS, effects);
        tag.putInt(TAG_HUNGER, hunger);
        tag.putFloat(TAG_SATURATION, saturation);
    }

    public boolean add(Level level, BlockPos pos, BlockState state, ItemStack food) {
        if (!food.isEdible()) return false;

        FoodProperties foodComponent = food.getItem().getFoodProperties();
        if (foodComponent == null) return false;

        int portions = getRemainingPortions();

        if (portions < CookingPots.maxPortions) {
            int foodHunger = foodComponent.getNutrition();

            float foodSaturationRatio = foodComponent.getSaturationModifier();
            float foodSaturation = Math.min((float)foodHunger * foodSaturationRatio * 2.0f, CookingPots.hungerRestored);

            ResourceLocation foodId = Registry.ITEM.getKey(food.getItem());

            if (foodId.toString().equals("minecraft:air")) {
                throw new IllegalStateException("Is this still happening?");
            }

            if (!contents.contains(foodId)) {
                contents.add(foodId);
            }

            // roll for each effect
            if (CookingPots.addFoodEffects) {
                List<Pair<MobEffectInstance, Float>> effects = foodComponent.getEffects();
                Map<MobEffectInstance, Float> map = new WeakHashMap<>();

                // try and get effects from sus stew
                if (food.getItem() instanceof SuspiciousStewItem) {
                    getSuspiciousStewEffects(food).forEach(instance -> map.put(instance, 1.0F));
                }

                // get effects from the food
                Random random = new Random();
                for (Pair<MobEffectInstance, Float> pair : effects) {
                    MobEffectInstance instance = pair.getFirst();
                    float chance = pair.getSecond();
                    map.put(instance, chance);
                }

                // try and add each effect according to its chance
                map.forEach((instance, chance) -> {
                    // don't double up on effects
                    if (this.effects.stream().anyMatch(e -> e.getEffect() == instance.getEffect())) return;

                    if (!this.effects.contains(instance) && random.nextFloat() < chance) {
                        this.effects.add(instance);
                    }
                });
            }

            hunger += foodHunger;
            saturation += foodSaturation;

            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Food provides hunger: " + foodHunger);
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Food provides saturation ratio: " + foodSaturationRatio);
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Food provides saturation: " + foodSaturation);
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Pot hunger is now: " + hunger);
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Pot saturation is now: " + saturation);

            setChanged();

            food.shrink(1);
            level.setBlockAndUpdate(pos, state.setValue(CookingPotBlock.LIQUID, 2));
            return true;
        }

        return false;
    }

    @Nullable
    public ItemStack take(Level level, BlockPos pos, BlockState state, ItemStack container) {
        // might support other containers in future
        if (container.getItem() != Items.BOWL) return null;

        int portions = getRemainingPortions();

        if (portions > 0) {
            // reduce pot's hunger and saturation
            float hr = CookingPots.hungerRestored / (float)hunger;
            float sr = saturation * hr;
            int newHunger = hunger - CookingPots.hungerRestored;
            float newSaturation = Math.round((saturation - sr) * 10.0) / 10.0F;
            float stewSaturation = Math.min(20, Math.max(0F, 2 * (Math.round(sr/2))));

            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Set pot hunger from " + hunger + " to " + newHunger + " (hr = " + hr + ")");
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Set pot saturation from " + saturation + " to " + newSaturation + " (sr = " + sr + ")");
            LogHelper.debug(Strange.MOD_ID, this.getClass(), "Set stew saturation to " + stewSaturation);

            hunger = newHunger;
            saturation = newSaturation;

            // create a stew from the pot's saturation and effects
            ItemStack stew = new ItemStack(CookingPots.MIXED_STEW);
            MixedStewItem.setSaturation(stew, stewSaturation);
            MixedStewItem.setEffects(stew, effects);
            container.shrink(1);

            // if no more portions in the pot, flush out the pot data
            if (hunger <= 0) {
                this.flush(level, pos, state);
                LogHelper.debug(Strange.MOD_ID, this.getClass(), "Hunger is 0, flushing pot");
            }

            // match cooking pot name if set
            if (!name.isEmpty()) {
                stew.setHoverName(new TextComponent(name));
            }

            setChanged();
            return stew;
        }

        return null;
    }

    public int getRemainingPortions() {
        return (int)Math.floor(hunger / (float) CookingPots.hungerRestored);
    }

    public static <T extends CookingPotBlockEntity> void tick(Level level, BlockPos pos, BlockState state, T pot) {
        if (level == null || level.getGameTime() % 20 == 0) return;

        Random random = level.getRandom();
        BlockState belowState = level.getBlockState(pos.below());
        Block belowBlock = belowState.getBlock();

        if (belowBlock == Blocks.FIRE
            || belowBlock == Blocks.SOUL_FIRE
            || belowBlock == Blocks.MAGMA_BLOCK
            || belowBlock == Blocks.LAVA
            || (belowBlock == Blocks.CAMPFIRE && belowState.getValue(CampfireBlock.LIT))
            || (belowBlock == Blocks.SOUL_CAMPFIRE && belowState.getValue(CampfireBlock.LIT))
        ) {
            if (!state.getValue(CookingPotBlock.HAS_FIRE)) {
                level.setBlock(pos, state.setValue(CookingPotBlock.HAS_FIRE, true), 3);
            }
        } else {
            if (state.getValue(CookingPotBlock.HAS_FIRE)) {
                level.setBlock(pos, state.setValue(CookingPotBlock.HAS_FIRE, false), 3);
            }
        }

        if (random.nextInt(1000) == 0) {
            pot.removeRandomEffect(random);
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, StrangeSounds.FERMENT, SoundSource.BLOCKS, 0.3F + (0.1F * random.nextFloat()), random.nextFloat() * 0.7F + 0.6F);
        }
    }

    public void removeRandomEffect(Random random) {
        if (!effects.isEmpty()) {
            effects.remove(random.nextInt(effects.size()));
        }
    }

    public void flush(Level level, BlockPos pos, BlockState state) {
        contents = new ArrayList<>();
        effects = new ArrayList<>();
        hunger = 0;
        saturation = 0;

        setChanged();

        level.setBlock(pos, state.setValue(CookingPotBlock.LIQUID, 0), 2);
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

    private List<MobEffectInstance> getSuspiciousStewEffects(ItemStack stew) {
        List<MobEffectInstance> effects = new ArrayList<>();

        // some copypasta from SuspiciousStew#finishUsingItem
        CompoundTag tag = stew.getTag();
        if (tag != null && tag.contains(SuspiciousStewItem.EFFECTS_TAG, 9)) {
            ListTag listTag = tag.getList(SuspiciousStewItem.EFFECTS_TAG, 10);
            for (int i = 0; i < listTag.size(); ++i) {
                MobEffect mobEffect;
                int duration = 160;
                CompoundTag effectTag = listTag.getCompound(i);
                if (effectTag.contains(SuspiciousStewItem.EFFECT_DURATION_TAG, 3)) {
                    duration = effectTag.getInt(SuspiciousStewItem.EFFECT_DURATION_TAG);
                }
                if ((mobEffect = MobEffect.byId(effectTag.getByte(SuspiciousStewItem.EFFECT_ID_TAG))) == null) continue;
                effects.add(new MobEffectInstance(mobEffect, duration, 0));
            }
        }

        return effects;
    }
}