package svenhjol.strange.enchantments.enchantment;

import net.minecraft.enchantment.EnchantmentType;
import svenhjol.meson.MesonModule;

public class RepelEnchantment extends BaseTreasureEnchantment
{
    public RepelEnchantment(MesonModule module)
    {
        super(module,"repel", Rarity.VERY_RARE, EnchantmentType.WEAPON);
    }

    @Override
    public int getMaxLevel()
    {
        return 3;
    }
}
