package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.Arrays;
import java.util.List;

public class Helmet implements IRelicItem {

    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:fire_protection",
            "minecraft:projectile_protection",
            "minecraft:blast_protection",
            "minecraft:protection",
            "minecraft:thorns",
            "minecraft:respiration",
            "minecraft:unbreaking",
            "minecraft:aqua_affinity"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.DIAMOND_HELMET);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return Relics.extraLevels;
    }
}
