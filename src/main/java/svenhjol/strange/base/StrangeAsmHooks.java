package svenhjol.strange.base;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.ItemStack;
import svenhjol.strange.enchantments.enchantment.BaseTreasureEnchantment;
import svenhjol.strange.enchantments.module.TreasureEnchantments;

import java.util.List;

public class StrangeAsmHooks {
    public static boolean canApplyEnchantments(List<EnchantmentData> enchantments, ItemStack stack) {
        boolean canApply = true;
        for (EnchantmentData ench : enchantments) {
            canApply = canApply && canApplyEnchantment(ench.enchantment, stack);
        }
        return canApply;
    }

    public static boolean canApplyEnchantment(Enchantment enchantment, ItemStack stack) {
        if (!TreasureEnchantments.obtainable) {
            return !(enchantment instanceof BaseTreasureEnchantment);
        }
        return true;
    }
}
