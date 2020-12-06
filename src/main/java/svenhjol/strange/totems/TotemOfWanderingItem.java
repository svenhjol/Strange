package svenhjol.strange.totems;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.CharmItem;

public class TotemOfWanderingItem extends CharmItem {
    public TotemOfWanderingItem(CharmModule module) {
        super(module, "totem_of_wandering", new Item.Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxCount(1));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
