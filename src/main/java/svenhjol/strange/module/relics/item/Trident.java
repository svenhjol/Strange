package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.Arrays;
import java.util.List;

public class Trident implements IRelicItem {
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
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return Relics.extraLevels;
    }
}
