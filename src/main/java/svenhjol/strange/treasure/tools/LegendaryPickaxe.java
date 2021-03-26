package svenhjol.strange.treasure.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.treasure.ITreasureTool;
import svenhjol.strange.treasure.Treasure;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LegendaryPickaxe implements ITreasureTool {
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
        return new ItemStack(new Random().nextFloat() < 0.5F ? Items.DIAMOND_PICKAXE : Items.IRON_PICKAXE);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return Treasure.extraLevels;
    }
}
