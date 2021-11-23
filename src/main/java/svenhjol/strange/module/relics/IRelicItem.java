package svenhjol.strange.module.relics;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import svenhjol.strange.module.colored_glints.ColoredGlints;

import java.util.*;

public interface IRelicItem {
    int MAX_ENCHANT_LEVEL = 10;

    List<String> getValidEnchantments();

    ItemStack getItemStack();

    int getMaxAdditionalLevels();

    default boolean isDamaged() {
        return true;
    }

    default TranslatableComponent getName(ItemStack itemStack) {
        int i = new Random().nextInt(16) + 1;
        Component word = new TranslatableComponent("item.strange.relics.adjective" + i);
        return new TranslatableComponent("item.strange.relics.itemname", word, itemStack.getHoverName());
    }

    default DyeColor getColor() {
        List<DyeColor> possibleColors = getPossibleColors();
        return possibleColors.get(new Random().nextInt(possibleColors.size()));
    }

    default List<DyeColor> getPossibleColors() {
        List<DyeColor> dyeColors = new ArrayList<>(Arrays.asList(DyeColor.values()));
        dyeColors.remove(ColoredGlints.getDefaultGlintColor());
        return dyeColors;
    }

    default Map<Enchantment, Integer> getEnchantments() {
        List<String> validEnchantments = getValidEnchantments();
        Random random = new Random();
        HashMap<Enchantment, Integer> map = new HashMap<>();

        String enchantmentName = validEnchantments.get(random.nextInt(validEnchantments.size()));
        Optional<Enchantment> optionalEnchantment = Registry.ENCHANTMENT.getOptional(new ResourceLocation(enchantmentName));
        if (optionalEnchantment.isEmpty()) return map;

        Enchantment enchantment = optionalEnchantment.get();

        int newLevel = Math.min(MAX_ENCHANT_LEVEL, enchantment.getMaxLevel() + random.nextInt(getMaxAdditionalLevels()) + 1);
        map.put(enchantment, newLevel);
        return map;
    }

    default ItemStack getRelicItem() {
        ItemStack itemStack = getItemStack();

        // apply custom name
        itemStack.setHoverName(getName(itemStack));

        // apply the enchantments
        EnchantmentHelper.setEnchantments(getEnchantments(), itemStack);

        // apply glint color
        ColoredGlints.applyColoredGlint(itemStack, getColor().getSerializedName().toLowerCase(Locale.ROOT));

        return itemStack;
    }
}
