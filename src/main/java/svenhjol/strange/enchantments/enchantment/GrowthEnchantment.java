package svenhjol.strange.enchantments.enchantment;

import net.minecraft.enchantment.EnchantmentType;
import svenhjol.meson.MesonEnchantment;
import svenhjol.meson.MesonModule;

public class GrowthEnchantment extends MesonEnchantment
{
    public GrowthEnchantment(MesonModule module)
    {
        super(module,"growth", Rarity.VERY_RARE, EnchantmentType.DIGGER);
    }
}
