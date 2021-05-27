package svenhjol.strange.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rarity;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.item.CharmItem;

public class TotemOfFlyingItem extends CharmItem {
    public TotemOfFlyingItem(CharmModule module) {
        super(module, "totem_of_flying", new Item.Settings()
            .group(ItemGroup.MISC)
            .rarity(Rarity.RARE)
            .maxCount(1));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
