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

public class NeedleSword implements ITreasureTool {
    @Override
    public DyeColor getColor() {
        return DyeColor.BLACK;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        HashMap<Enchantment, Integer> map = new HashMap<>();
        map.put(Enchantments.SHARPNESS, 3);
        map.put(Enchantments.SMITE, 3);
        map.put(Enchantments.BANE_OF_ARTHROPODS, 3);
        return map;
    }

    @Override
    public List<String> getValidEnchantments() {
        return null;
    }

    @Override
    public TranslatableComponent getName(ItemStack itemStack) {
        return new TranslatableComponent("item.strange.treasure.needle_sword");
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.DIAMOND_SWORD);
    }

    @Override
    public int getMaxAdditionalLevels() {
        return 0;
    }
}
