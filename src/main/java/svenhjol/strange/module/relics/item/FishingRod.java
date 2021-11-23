package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.Arrays;
import java.util.List;

public class FishingRod implements IRelicItem {

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
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return Relics.extraLevels;
    }
}
