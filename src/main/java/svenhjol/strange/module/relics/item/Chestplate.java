package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.Arrays;
import java.util.List;

public class Chestplate implements IRelicItem {
    @Override
    public Relics.Type getType() {
        return Relics.Type.ARMOR;
    }

    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:fire_protection",
            "minecraft:projectile_protection",
            "minecraft:blast_protection",
            "minecraft:protection",
            "minecraft:unbreaking",
            "minecraft:thorns"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.DIAMOND_CHESTPLATE);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return Relics.extraLevels;
    }
}
