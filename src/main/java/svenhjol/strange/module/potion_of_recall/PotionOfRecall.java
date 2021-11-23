package svenhjol.strange.module.potion_of_recall;

import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

@CommonModule(mod = Strange.MOD_ID)
public class PotionOfRecall extends CharmModule {
    public static RecallPotion RECALL_POTION;
    public static RecallEffect RECALL_EFFECT;

    @Override
    public void register() {
        RECALL_EFFECT = new RecallEffect(this);
        RECALL_POTION = new RecallPotion(this);
    }
}
