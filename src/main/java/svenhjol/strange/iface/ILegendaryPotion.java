package svenhjol.strange.iface;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import svenhjol.charm.base.helper.PotionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface ILegendaryPotion {
    List<StatusEffect> getValidStatusEffects();

    // in seconds
    int getMinDuration();

    // in seconds
    int getMaxDuration();

    int getMinAmplifier();

    int getMaxAmplifier();

    default TranslatableText getName() {
        int i = new Random().nextInt(16) + 1;
        Text word = new TranslatableText("item.strange.legendary.potion.adjective" + i);
        return new TranslatableText("item.strange.legendary.potion", word);
    }

    default List<StatusEffectInstance> getEffects() {
        List<StatusEffectInstance> instances = new ArrayList<>();
        List<StatusEffect> statusEffects = getValidStatusEffects();
        Random random = new Random();

        int minDuration = getMinDuration();
        int maxDuration = getMaxDuration();

        int minAmplifier = getMinAmplifier();
        int maxAmplifier = getMaxAmplifier();

        statusEffects.forEach(effect -> {
            int duration = (random.nextInt(maxDuration - minDuration) + minDuration) * 20; // in ticks
            int amplifier = random.nextInt(maxAmplifier - minAmplifier) + minAmplifier;
            instances.add(new StatusEffectInstance(effect, duration, amplifier));
        });

        return instances;
    }

    default ItemStack getTreasurePotion() {
        ItemStack bottle = PotionHelper.getFilledWaterBottle();

        // apply effects
        PotionUtil.setCustomPotionEffects(bottle, getEffects());

        // apply custom name
        bottle.setCustomName(getName());

        return bottle;
    }
}
