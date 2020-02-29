package svenhjol.strange.enchantments.enchantment;

import net.minecraft.enchantment.EnchantmentType;
import svenhjol.meson.MesonModule;

public class IceAspectEnchantment extends BaseTreasureEnchantment
{
    public IceAspectEnchantment(MesonModule module)
    {
        super(module,"ice_aspect", Rarity.VERY_RARE, EnchantmentType.WEAPON);
    }

    @Override
    public int getMaxLevel()
    {
        return 2;
    }
}
