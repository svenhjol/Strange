package svenhjol.strange.iface;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import svenhjol.charm.handler.ColoredGlintHandler;

import java.util.*;

public interface ILegendaryEnchanted {
    List<String> getValidEnchantments();

    ItemStack getItemStack();

    int getMaxAdditionalLevels();

    default List<DyeColor> getPossibleColors() {
        return Arrays.asList(DyeColor.values());
    }

    default boolean damaged() {
        return true;
    }

    default TranslatableText getName(ItemStack itemStack) {
        int i = new Random().nextInt(16) + 1;
        Text word = new TranslatableText("item.strange.legendary.enchanted.adjective" + i);
        return new TranslatableText("item.strange.legendary.enchanted", word, itemStack.getName());
    }

    default DyeColor getColor() {
        List<DyeColor> possibleColors = getPossibleColors();
        return possibleColors.get(new Random().nextInt(possibleColors.size()));
    }

    default Map<Enchantment, Integer> getEnchantments() {
        List<String> validEnchantments = getValidEnchantments();
        Random random = new Random();
        HashMap<Enchantment, Integer> map = new HashMap<>();

        String enchantmentName = validEnchantments.get(random.nextInt(validEnchantments.size()));
        Optional<Enchantment> optionalEnchantment = Registry.ENCHANTMENT.getOrEmpty(new Identifier(enchantmentName));
        if (!optionalEnchantment.isPresent())
            return map;

        Enchantment enchantment = optionalEnchantment.get();

        int newLevel = Math.min(10, enchantment.getMaxLevel() + random.nextInt(getMaxAdditionalLevels()) + 1);
        map.put(enchantment, newLevel);
        return map;
    }

    default ItemStack getTreasureItemStack() {
        ItemStack itemStack = getItemStack();

        // apply custom name
        itemStack.setCustomName(getName(itemStack));

        // apply the enchantments
        EnchantmentHelper.set(getEnchantments(), itemStack);

        // apply glint color
        itemStack.getOrCreateTag().putString(ColoredGlintHandler.GLINT_TAG, getColor().getName());

        return itemStack;
    }
}
