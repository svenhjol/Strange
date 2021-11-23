package svenhjol.strange.module.potion_of_recall;

import net.minecraft.world.effect.MobEffectInstance;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.potion.CharmPotion;

public class RecallPotion extends CharmPotion {
    public RecallPotion(CharmModule module) {
        super(module, "recall", new MobEffectInstance(PotionOfRecall.RECALL_EFFECT, 20));
    }
}
