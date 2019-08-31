package svenhjol.strange.totems.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.strange.totems.module.TotemOfAttracting;

public class TotemOfAttractingItem extends MesonItem
{
    public TotemOfAttractingItem(MesonModule module)
    {
        super(module, "totem_of_attracting", new Item.Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfAttracting.durability)
        );
    }
}
