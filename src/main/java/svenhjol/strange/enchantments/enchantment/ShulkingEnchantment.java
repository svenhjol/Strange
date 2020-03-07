package svenhjol.strange.enchantments.enchantment;

import net.minecraft.enchantment.EnchantmentType;
import svenhjol.meson.MesonModule;

public class ShulkingEnchantment extends BaseTreasureEnchantment {
    public ShulkingEnchantment(MesonModule module) {
        super(module, "shulking", Rarity.VERY_RARE, EnchantmentType.WEAPON);
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
}
