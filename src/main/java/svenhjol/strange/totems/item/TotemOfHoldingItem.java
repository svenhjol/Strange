package svenhjol.strange.totems.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.totems.module.TotemOfHolding;

public class TotemOfHoldingItem extends MesonItem
{
    public TotemOfHoldingItem(MesonModule module)
    {
        super(module, "totem_of_holding", new Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfHolding.durability)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }
}
