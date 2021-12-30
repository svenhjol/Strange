package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.Arrays;
import java.util.List;

public class Sword implements IRelicItem {
    @Override
    public Relics.Type getType() {
        return Relics.Type.WEAPON;
    }

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
        return new ItemStack(Items.DIAMOND_SWORD);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return Relics.extraLevels;
    }
}
