package svenhjol.strange.runestones.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;

public class AmethystItem extends MesonItem {
    public AmethystItem(MesonModule module) {
        super(module, "amethyst", new Item.Properties()
            .group(ItemGroup.MISC)
            .maxStackSize(1)
        );
    }
}
