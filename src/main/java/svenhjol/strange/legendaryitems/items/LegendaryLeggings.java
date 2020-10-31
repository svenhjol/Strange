package svenhjol.strange.legendaryitems.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.legendaryitems.ILegendaryEnchanted;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LegendaryLeggings implements ILegendaryEnchanted {

    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:fire_protection",
            "minecraft:projectile_protection",
            "minecraft:blast_protection",
            "minecraft:protection",
            "minecraft:unbreaking",
            "minecraft:thorns"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(new Random().nextFloat() < 0.5F ? Items.DIAMOND_LEGGINGS : Items.IRON_LEGGINGS);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return 3;
    }
}
