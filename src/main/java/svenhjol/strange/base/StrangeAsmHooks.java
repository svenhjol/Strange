package svenhjol.strange.base;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import svenhjol.strange.enchantments.enchantment.BaseTreasureEnchantment;
import svenhjol.strange.enchantments.module.TreasureEnchantments;

import java.util.List;

public class StrangeAsmHooks {
    public static boolean canApplyEnchantments(List<EnchantmentData> enchantments) {
        boolean canApply = true;
        for (EnchantmentData ench : enchantments) {
            canApply = canApply && canApplyEnchantment(ench.enchantment);
        }
        return canApply;
    }

    public static boolean canApplyEnchantment(Enchantment enchantment) {
        if (!TreasureEnchantments.obtainable) {
            return !(enchantment instanceof BaseTreasureEnchantment);
        }
        return true;
    }
}
