package svenhjol.strange.module.treasure.tool;

import svenhjol.strange.module.treasure.ITreasureTool;
import svenhjol.strange.module.treasure.Treasure;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LegendarySword implements ITreasureTool {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:sharpness",
            "minecraft:smite",
            "minecraft:bane_of_arthropods",
            "minecraft:knockback",
            "minecraft:fire_aspect",
            "minecraft:looting",
            "minecraft:unbreaking",
            "minecraft:sweeping"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(new Random().nextFloat() < 0.5F ? Items.DIAMOND_SWORD : Items.IRON_SWORD);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return Treasure.extraLevels;
    }
}
