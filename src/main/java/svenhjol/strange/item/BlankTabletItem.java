package svenhjol.strange.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import svenhjol.charm.base.CharmModule;

public class BlankTabletItem extends TabletItem {
    public BlankTabletItem(CharmModule module, String name) {
        super(module, name, new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }
}
