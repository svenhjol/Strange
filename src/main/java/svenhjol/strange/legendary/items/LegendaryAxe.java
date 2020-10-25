package svenhjol.strange.legendary.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.iface.ILegendaryEnchanted;

import java.util.Arrays;
import java.util.List;

public class LegendaryAxe implements ILegendaryEnchanted {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:sharpness",
            "minecraft:smite",
            "minecraft:bane_of_arthropods",
            "minecraft:knockback",
            "minecraft:cleaving",
            "minecraft:looting",
            "minecraft:unbreaking",
            "minecraft:fortune"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.DIAMOND_AXE);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return 3;
    }
}
