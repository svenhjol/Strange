package svenhjol.strange.legendaryitems.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.legendaryitems.ILegendaryEnchanted;
import svenhjol.strange.legendaryitems.LegendaryItems;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LegendarySword implements ILegendaryEnchanted {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:sharpness",
            "minecraft:smite",
            "minecraft:bane_of_arthropods",
            "minecraft:knockback",
            "minecraft:fire_aspect",
            "minecraft:looting",
            "minecraft:unbreaking",
            "minecraft:sweeping"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(new Random().nextFloat() < 0.5F ? Items.DIAMOND_SWORD : Items.IRON_SWORD);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return LegendaryItems.extraLevels;
    }
}
