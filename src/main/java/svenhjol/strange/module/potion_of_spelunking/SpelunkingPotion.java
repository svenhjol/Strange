package svenhjol.strange.module.potion_of_spelunking;

import net.minecraft.world.effect.MobEffectInstance;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.potion.CharmPotion;

public class SpelunkingPotion extends CharmPotion {
    public SpelunkingPotion(CharmModule module) {
        super(module, "spelunking", new MobEffectInstance(PotionOfSpelunking.SPELUNKING_EFFECT, PotionOfSpelunking.duration * 20));
    }
}
