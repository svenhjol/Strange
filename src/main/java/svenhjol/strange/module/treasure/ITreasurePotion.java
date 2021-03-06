package svenhjol.strange.module.treasure;

import svenhjol.charm.helper.PotionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;

public interface ITreasurePotion {
    List<MobEffect> getValidStatusEffects();

    // in seconds
    int getMinDuration();

    // in seconds
    int getMaxDuration();

    int getMinAmplifier();

    int getMaxAmplifier();

    default TranslatableComponent getName() {
        int i = new Random().nextInt(16) + 1;
        Component word = new TranslatableComponent("item.strange.treasure.potion.adjective" + i);
        return new TranslatableComponent("item.strange.treasure.potion", word);
    }

    default List<MobEffectInstance> getEffects() {
        List<MobEffectInstance> instances = new ArrayList<>();
        List<MobEffect> statusEffects = getValidStatusEffects();
        Random random = new Random();

        int minDuration = getMinDuration();
        int maxDuration = getMaxDuration();

        int minAmplifier = getMinAmplifier();
        int maxAmplifier = getMaxAmplifier();

        statusEffects.forEach(effect -> {
            int duration = (random.nextInt(maxDuration - minDuration) + minDuration) * 20; // in ticks
            int amplifier = random.nextInt(maxAmplifier - minAmplifier) + minAmplifier;
            instances.add(new MobEffectInstance(effect, duration, amplifier));
        });

        return instances;
    }

    default ItemStack getTreasurePotion() {
        ItemStack bottle = PotionHelper.getFilledWaterBottle();

        // apply effects
        PotionUtils.setCustomEffects(bottle, getEffects());

        // apply custom name
        bottle.setHoverName(getName());

        return bottle;
    }
}
