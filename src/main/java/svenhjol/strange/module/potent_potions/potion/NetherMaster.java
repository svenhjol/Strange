package svenhjol.strange.module.potent_potions.potion;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import svenhjol.strange.Strange;
import svenhjol.strange.module.potent_potions.IPotionItem;
import svenhjol.strange.module.potion_of_hogsbane.PotionOfHogsbane;

import java.util.Arrays;
import java.util.List;

public class NetherMaster implements IPotionItem {
    @Override
    public List<MobEffect> getValidStatusEffects() {
        return Arrays.asList(
            MobEffects.FIRE_RESISTANCE,
            MobEffects.NIGHT_VISION,
            MobEffects.REGENERATION
        );
    }

    @Override
    public List<MobEffectInstance> getEffects() {
        List<MobEffectInstance> effects = IPotionItem.super.getEffects();

        if (Strange.LOADER.isEnabled(PotionOfHogsbane.class)) {
            effects.add(new MobEffectInstance(PotionOfHogsbane.HOGSBANE_EFFECT, getMaxDuration() * 20, 0));
        }

        return effects;
    }

    @Override
    public TranslatableComponent getName() {
        return new TranslatableComponent("item.strange.potent_potions.nether_master");
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
        return 1;
    }

    @Override
    public int getMaxAmplifier() {
        return 1;
    }
}
