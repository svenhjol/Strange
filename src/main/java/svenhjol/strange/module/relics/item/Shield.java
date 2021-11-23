package svenhjol.strange.module.relics.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.List;

public class Shield implements IRelicItem {
    @Override
    public List<String> getValidEnchantments() {
        return List.of(
            "minecraft:unbreaking"
        );
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.SHIELD);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return Relics.extraLevels;
    }
}
