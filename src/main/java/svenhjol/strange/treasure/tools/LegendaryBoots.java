package svenhjol.strange.treasure.tools;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import svenhjol.strange.treasure.ITreasureTool;
import svenhjol.strange.treasure.Treasure;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LegendaryBoots implements ITreasureTool {

    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:fire_protection",
            "minecraft:projectile_protection",
            "minecraft:blast_protection",
            "minecraft:protection",
            "minecraft:feather_falling",
            "minecraft:thorns",
            "minecraft:depth_strider",
            "minecraft:soul_speed",
            "minecraft:unbreaking",
            "minecraft:frost_walker"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(new Random().nextFloat() < 0.5F ? Items.DIAMOND_BOOTS : Items.IRON_BOOTS);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return Treasure.extraLevels;
    }
}
