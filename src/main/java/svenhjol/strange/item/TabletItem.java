package svenhjol.strange.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Rarity;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.CharmItem;

public class TabletItem extends CharmItem {
    public TabletItem(CharmModule module, String name) {
        super(module, name, new Item.Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }
}
