package svenhjol.strange.module.treasure.tool;

import svenhjol.strange.module.treasure.ITreasureTool;
import svenhjol.strange.module.treasure.Treasure;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class LegendaryShield implements ITreasureTool {
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
        return Treasure.extraLevels;
    }
}
