package svenhjol.strange.enchantments.enchantment;

import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import svenhjol.meson.MesonEnchantment;
import svenhjol.meson.MesonModule;
import svenhjol.strange.enchantments.module.TreasureEnchantments;

public abstract class BaseTreasureEnchantment extends MesonEnchantment
{
    public BaseTreasureEnchantment(MesonModule module, String name, Rarity rarity, EnchantmentType type, EquipmentSlotType... slots)
    {
        super(module, name, rarity, type, slots);
    }

    @Override
    public boolean canApply(ItemStack stack)
    {
        return TreasureEnchantments.obtainable;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack)
    {
        return TreasureEnchantments.obtainable;
    }

    @Override
    public int getMaxLevel()
    {
        return 1;
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel)
    {
        return TreasureEnchantments.obtainable ? 30 : 255;
    }

    @Override
    public boolean isTreasureEnchantment()
    {
        return true;
    }

    @Override
    public boolean isAllowedOnBooks()
    {
        return TreasureEnchantments.obtainable;
    }
}
