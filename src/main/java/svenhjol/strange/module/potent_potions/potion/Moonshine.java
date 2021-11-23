package svenhjol.strange.module.potent_potions.potion;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import svenhjol.strange.module.potent_potions.IPotionItem;

import java.util.Arrays;
import java.util.List;

public class Moonshine implements IPotionItem {
    @Override
    public List<MobEffect> getValidStatusEffects() {
        return Arrays.asList(
            MobEffects.REGENERATION,
            MobEffects.HEALTH_BOOST
        );
    }

    @Override
    public List<MobEffectInstance> getEffects() {
        List<MobEffectInstance> effects = IPotionItem.super.getEffects();
        effects.add(new MobEffectInstance(MobEffects.BLINDNESS, (getMinDuration() / 3) * 20, 0));
        effects.add(new MobEffectInstance(MobEffects.CONFUSION, (getMinDuration() / 3) * 20, 0));
        return effects;
    }

    @Override
    public TranslatableComponent getName() {
        return new TranslatableComponent("item.strange.potent_potions.moonshine");
    }

    @Override
    public int getMinDuration() {
        return 100;
    }

    @Override
    public int getMaxDuration() {
        return 200;
    }

    @Override
    public int getMinAmplifier() {
        return 3;
    }

    @Override
    public int getMaxAmplifier() {
        return 5;
    }
}
