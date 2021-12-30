package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.Arrays;
import java.util.List;

public class Shovel implements IRelicItem {
    @Override
    public Relics.Type getType() {
        return Relics.Type.TOOL;
    }

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
        return new ItemStack(Items.DIAMOND_SHOVEL);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return Relics.extraLevels;
    }
}
