package svenhjol.strange.legendary.items;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import svenhjol.strange.iface.ILegendaryEnchanted;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AngeryPotato implements ILegendaryEnchanted {
    @Override
    public DyeColor getColor() {
        return DyeColor.ORANGE;
    }

    @Override
    public List<String> getValidEnchantments() {
        return null;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        HashMap<Enchantment, Integer> map = new HashMap<>();
        map.put(Enchantments.KNOCKBACK, 1);
        map.put(Enchantments.FIRE_ASPECT, 1);
        return map;
    }

    @Override
    public TranslatableText getName(ItemStack itemStack) {
        return new TranslatableText("item.strange.legendary.angery_potato");
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.POTATO);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return 0;
    }
}
