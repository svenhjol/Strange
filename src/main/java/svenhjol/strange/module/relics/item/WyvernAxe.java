package svenhjol.strange.module.relics.item;

import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import svenhjol.strange.module.relics.IRelicItem;
import svenhjol.strange.module.relics.Relics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WyvernAxe implements IRelicItem {
    @Override
    public Relics.Type getType() {
        return Relics.Type.WEAPON;
    }

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
        return new TranslatableComponent("item.strange.relics.wyvern_axe");
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.DIAMOND_AXE);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return 0;
    }
}
