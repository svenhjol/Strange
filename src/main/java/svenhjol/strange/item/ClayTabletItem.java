package svenhjol.strange.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import svenhjol.charm.base.CharmModule;

public class ClayTabletItem extends TabletItem {
    public ClayTabletItem(CharmModule module, String name) {
        super(module, name, new Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.COMMON)
            .maxCount(1));
    }
}
