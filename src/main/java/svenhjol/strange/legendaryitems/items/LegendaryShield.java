package svenhjol.strange.legendaryitems.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.legendaryitems.ILegendaryEnchanted;
import svenhjol.strange.module.LegendaryItems;

import java.util.Arrays;
import java.util.List;

public class LegendaryShield implements ILegendaryEnchanted {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:unbreaking"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.SHIELD);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return LegendaryItems.extraLevels;
    }
}
