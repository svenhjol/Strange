package svenhjol.strange.module.treasure;

import svenhjol.charm.module.colored_glints.ColoredGlintHandler;

import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public interface ITreasureTool {
    List<String> getValidEnchantments();

    ItemStack getItemStack();

    int getMaxAdditionalLevels();

    default List<DyeColor> getPossibleColors() {
        return Arrays.asList(DyeColor.values());
    }

    default boolean damaged() {
        return true;
    }

    default TranslatableComponent getName(ItemStack itemStack) {
        int i = new Random().nextInt(16) + 1;
        Component word = new TranslatableComponent("item.strange.treasure.enchanted.adjective" + i);
        return new TranslatableComponent("item.strange.treasure.enchanted", word, itemStack.getHoverName());
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
        Optional<Enchantment> optionalEnchantment = Registry.ENCHANTMENT.getOptional(new ResourceLocation(enchantmentName));
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
        itemStack.setHoverName(getName(itemStack));

        // apply the enchantments
        EnchantmentHelper.setEnchantments(getEnchantments(), itemStack);

        // apply glint color
        itemStack.getOrCreateTag().putString(ColoredGlintHandler.GLINT_NBT, getColor().getName());

        return itemStack;
    }
}
