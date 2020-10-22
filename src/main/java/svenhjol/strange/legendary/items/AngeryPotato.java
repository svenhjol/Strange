package svenhjol.strange.legendary.items;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import svenhjol.strange.iface.ILegendaryEnchanted;

import java.util.Arrays;
import java.util.List;

public class AngeryPotato implements ILegendaryEnchanted {
    @Override
    public List<String> getValidEnchantments() {
        return Arrays.asList(
            "minecraft:knockback",
            "minecraft:fire_aspect"
        );
    }

    @Override
    public TranslatableText getName(ItemStack itemStack) {
        return new TranslatableText("item.strange.legendary.angery_potato");
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.POTATO);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return 3;
    }
}
