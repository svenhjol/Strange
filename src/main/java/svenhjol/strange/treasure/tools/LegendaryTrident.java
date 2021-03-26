package svenhjol.strange.treasure.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.treasure.ITreasureTool;
import svenhjol.strange.treasure.Treasure;

import java.util.Arrays;
import java.util.List;

public class LegendaryTrident implements ITreasureTool {
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
        return Treasure.extraLevels;
    }
}
