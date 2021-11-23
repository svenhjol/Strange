package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class PickaxeRelic implements IRelicItem {
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
        return new ItemStack(Items.DIAMOND_PICKAXE);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return Relics.extraLevels;
    }
}
