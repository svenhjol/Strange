package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.QuickChargeEnchantment;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.Arrays;
import java.util.List;

public class Crossbow implements IRelicItem {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:quick_charge",
            "minecraft:piercing",
            "minecraft:unbreaking"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.CROSSBOW);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        if (enchantment instanceof QuickChargeEnchantment) {
            return 2; // don't go above Quick Charge 5.  Quick Charge 6 takes 70 years to reload...
        }

        return Relics.extraLevels;
    }
}
