package svenhjol.strange.totems.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.totems.module.TotemOfFlying;

public class TotemOfFlyingItem extends MesonItem
{
    public TotemOfFlyingItem(MesonModule module)
    {
        super(module, "totem_of_flying", new Item.Properties()
            .group(ItemGroup.TRANSPORTATION)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfFlying.durability)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }
}
