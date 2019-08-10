package svenhjol.strange.totems.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import svenhjol.meson.MesonItem;
import svenhjol.strange.Strange;
import svenhjol.strange.totems.feature.TotemOfShielding;

public class TotemOfShieldingItem extends MesonItem
{
    public TotemOfShieldingItem()
    {
        super(new Item.Properties()
            .group(ItemGroup.COMBAT)
            .rarity(Rarity.UNCOMMON)
            .maxStackSize(1)
            .maxDamage(TotemOfShielding.maxHealth.get())
        );
        setRegistryName(new ResourceLocation(Strange.MOD_ID, "totem_of_shielding"));
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }
}
