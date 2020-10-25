package svenhjol.strange.legendary.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.iface.ILegendaryEnchanted;

import java.util.Arrays;
import java.util.List;

public class LegendaryFishingRod implements ILegendaryEnchanted {
@Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:lure",
            "minecraft:luck_of_the_sea",
            "minecraft:unbreaking"
            );
    }

    @Override
    public ItemStack getItemStack() {
            return new ItemStack(Items.FISHING_ROD);
        }

    @Override
    public int getMaxAdditionalLevels() {return 3;
    }
}
