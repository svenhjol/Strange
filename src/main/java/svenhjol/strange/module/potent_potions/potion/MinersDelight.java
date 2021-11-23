package svenhjol.strange.module.potent_potions.potion;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import svenhjol.strange.Strange;
import svenhjol.strange.module.potent_potions.IPotionItem;

import java.util.Arrays;
import java.util.List;

public class MinersDelight implements IPotionItem {
    @Override
    public List<MobEffect> getValidStatusEffects() {
        return Arrays.asList(
            MobEffects.NIGHT_VISION,
            MobEffects.DIG_SPEED
        );
    }

    @Override
    public List<MobEffectInstance> getEffects() {
        List<MobEffectInstance> effects = IPotionItem.super.getEffects();

        if (Strange.LOADER.isEnabled("strange:potion_of_spelunking")) {
            // TODO: add spelunking effect here
        }

        return effects;
    }

    @Override
    public TranslatableComponent getName() {
        return new TranslatableComponent("item.strange.potent_potions.miners_delight");
    }

    @Override
    public int getMinDuration() {
        return 480;
    }

    @Override
    public int getMaxDuration() {
        return 960;
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
