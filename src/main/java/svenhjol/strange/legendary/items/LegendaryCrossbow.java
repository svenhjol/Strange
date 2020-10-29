package svenhjol.strange.legendary.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.iface.ILegendaryEnchanted;
import svenhjol.strange.module.LegendaryItems;

import java.util.Arrays;
import java.util.List;

public class LegendaryCrossbow implements ILegendaryEnchanted {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:quick_charge",
            "minecraft:piercing",
            "minecraft:unbreaking"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.CROSSBOW);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return LegendaryItems.extraLevels;
    }
}
