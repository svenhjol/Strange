package svenhjol.strange.totems.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import svenhjol.meson.MesonItem;
import svenhjol.strange.Strange;
import svenhjol.strange.totems.feature.TotemOfAttracting;

public class TotemOfAttractingItem extends MesonItem
{
    public TotemOfAttractingItem()
    {
        super(new Item.Properties()
            .group(ItemGroup.MISC)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfAttracting.maxHealth.get())
        );
        setRegistryName(new ResourceLocation(Strange.MOD_ID, "totem_of_attracting"));
    }
}
