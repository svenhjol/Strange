package svenhjol.strange.iface;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public interface ILegendaryEnchanted {
    List<String> getValidEnchantments();

    ItemStack getItemStack();

    int getMaxAdditionalLevels();

    default boolean damaged() {
        return true;
    }

    default TranslatableText getName(ItemStack itemStack) {
        int i = new Random().nextInt(16) + 1;
        Text word = new TranslatableText("item.strange.legendary.enchanted.adjective" + i);
        return new TranslatableText("item.strange.legendary.enchanted", word, itemStack.getName());
    }

    default Map<Enchantment, Integer> getEnchantments() {
        List<String> validEnchantments = getValidEnchantments();
        Random rand = new Random();
        HashMap<Enchantment, Integer> map = new HashMap<>();

        String enchantmentName = validEnchantments.get(rand.nextInt(validEnchantments.size()));
        Optional<Enchantment> optionalEnchantment = Registry.ENCHANTMENT.getOrEmpty(new Identifier(enchantmentName));
        if (!optionalEnchantment.isPresent())
            return map;

        Enchantment enchantment = optionalEnchantment.get();

        int newLevel = Math.min(10, enchantment.getMaxLevel() + rand.nextInt(getMaxAdditionalLevels()) + 1);
        map.put(enchantment, newLevel);
        return map;
    }

    default ItemStack getTreasureItemStack() {
        ItemStack itemStack = getItemStack();

        // apply custom name
        itemStack.setCustomName(getName(itemStack));

        // apply the enchantments
        EnchantmentHelper.set(getEnchantments(), itemStack);

        // TODO: color glint

        return itemStack;
    }
}
