package svenhjol.strange.totems.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.totems.module.TotemOfShielding;

public class TotemOfShieldingItem extends MesonItem {
    public TotemOfShieldingItem(MesonModule module) {
        super(module, "totem_of_shielding", new Item.Properties()
            .group(ItemGroup.COMBAT)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfShielding.durability)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
