package svenhjol.strange.module.relics.item;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import svenhjol.strange.module.relics.IRelicItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmbitiousCrossbow implements IRelicItem {
    @Override
    public DyeColor getColor() {
        return DyeColor.BLACK;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        HashMap<Enchantment, Integer> map = new HashMap<>();
        map.put(Enchantments.MULTISHOT, 1);
        map.put(Enchantments.PIERCING, 1);
        return map;
    }

    @Override
    public List<String> getValidEnchantments() {
        return null;
    }

    @Override
    public TranslatableComponent getName(ItemStack itemStack) {
        return new TranslatableComponent("item.strange.relics.ambitious_crossbow");
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.CROSSBOW);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return 0;
    }
}
