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

@SuppressWarnings("unused")
public class WaterPumpkin implements IRelicItem {
    @Override
    public Relics.Type getType() {
        return Relics.Type.WEIRD;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.LIGHT_BLUE;
    }

    @Override
    public boolean isDamaged() {
        return false;
    }

    @Override
    public List<String> getValidEnchantments() {
        return null;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        HashMap<Enchantment, Integer> map = new HashMap<>();
        map.put(Enchantments.RESPIRATION, 5);
        return map;
    }

    @Override
    public TranslatableComponent getName(ItemStack itemStack) {
        return new TranslatableComponent("item.strange.relics.water_pumpkin");
    }

    @Override
    public ItemStack getItemStack() {
        return new ItemStack(Items.CARVED_PUMPKIN);
    }

    @Override
    public int getMaxAdditionalLevels(Enchantment enchantment) {
        return 0;
    }
}
