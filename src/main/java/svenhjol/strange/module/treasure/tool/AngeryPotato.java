package svenhjol.strange.module.treasure.tool;

import svenhjol.strange.module.treasure.ITreasureTool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class AngeryPotato implements ITreasureTool {
    @Override
    public DyeColor getColor() {
        return DyeColor.ORANGE;
    }

    @Override
    public List<String> getValidEnchantments() {
        return null;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        HashMap<Enchantment, Integer> map = new HashMap<>();
        map.put(Enchantments.KNOCKBACK, 1);
        map.put(Enchantments.FIRE_ASPECT, 1);
        return map;
    }

    @Override
    public TranslatableComponent getName(ItemStack itemStack) {
        return new TranslatableComponent("item.strange.treasure.angery_potato");
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.POTATO);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return 0;
    }
}
