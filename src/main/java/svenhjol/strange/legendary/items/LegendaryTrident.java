package svenhjol.strange.legendary.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.iface.ILegendaryEnchanted;
import svenhjol.strange.module.LegendaryItems;

import java.util.Arrays;
import java.util.List;

public class LegendaryTrident implements ILegendaryEnchanted {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:loyalty",
            "minecraft:riptide",
            "minecraft:unbreaking",
            "minecraft:impaling"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.TRIDENT);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return LegendaryItems.extraLevels;
    }
}
