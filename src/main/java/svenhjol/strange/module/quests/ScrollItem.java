package svenhjol.strange.module.quests;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;

public class ScrollItem extends CharmItem {
    public ScrollItem(CharmModule module, int tier) {
        super(module, Quests.TIER_NAMES.get(tier) + "_scroll", new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .rarity(Rarity.COMMON)
            .stacksTo(1));
    }
}
