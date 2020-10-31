package svenhjol.strange.legendaryitems.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.legendaryitems.ILegendaryEnchanted;
import svenhjol.strange.module.LegendaryItems;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LegendaryShovel implements ILegendaryEnchanted {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:fortune",
            "minecraft:unbreaking",
            "minecraft:efficiency"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(new Random().nextFloat() < 0.5F ? Items.DIAMOND_SHOVEL : Items.IRON_SHOVEL);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return LegendaryItems.extraLevels;
    }
}
