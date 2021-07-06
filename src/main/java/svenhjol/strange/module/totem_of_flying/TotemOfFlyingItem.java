package svenhjol.strange.module.totem_of_flying;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import svenhjol.charm.item.CharmItem;
import svenhjol.charm.loader.CharmModule;

public class TotemOfFlyingItem extends CharmItem {
    public TotemOfFlyingItem(CharmModule module) {
        super(module, "totem_of_flying", new Item.Properties()
            .tab(CreativeModeTab.TAB_MISC)
            .rarity(Rarity.RARE)
            .stacksTo(1));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
