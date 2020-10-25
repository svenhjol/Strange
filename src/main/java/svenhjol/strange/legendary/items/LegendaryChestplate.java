package svenhjol.strange.legendary.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.iface.ILegendaryEnchanted;

import java.util.Arrays;
import java.util.List;

public class LegendaryChestplate implements ILegendaryEnchanted {

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
        return new ItemStack(Items.DIAMOND_CHESTPLATE);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return 3;
    }
}
