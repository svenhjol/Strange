package svenhjol.strange.enchantments.iface;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;

import java.util.Map;

public interface ITreasureEnchantment
{
    Map<Enchantment, Integer> getEnchantments();

    ItemStack getTreasureItem();

    DyeColor getColor();
}