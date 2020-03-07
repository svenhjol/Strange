package svenhjol.strange.enchantments.iface;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public interface ILegendaryItem extends ITreasureEnchantment {
    List<String> getValidEnchantments();

    ItemStack getItemStack();

    int getMaxAdditionalLevels();

    default Map<Enchantment, Integer> getEnchantments() {
        List<String> validEnchantments = getValidEnchantments();
        Random rand = new Random();

        String enchantmentName = validEnchantments.get(rand.nextInt(validEnchantments.size()));
        Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(enchantmentName));
        if (enchantment == null) return new HashMap<>();

        HashMap<Enchantment, Integer> map = new HashMap<>();
        int newLevel = Math.min(10, enchantment.getMaxLevel() + rand.nextInt(getMaxAdditionalLevels()) + 1);
        map.put(enchantment, newLevel);
        return map;
    }

    default ItemStack getTreasureItem() {
        ItemStack treasure = getItemStack();
        int i = new Random().nextInt(16) + 1;

        TranslationTextComponent word = new TranslationTextComponent("legendary.strange.word" + i);
        treasure.setDisplayName(new TranslationTextComponent("item.strange.treasure.legendary", word, treasure.getDisplayName()));
        return treasure;
    }

    default DyeColor getColor() {
        return DyeColor.byId(new Random().nextInt(13) + 2);
    }
}
