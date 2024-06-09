package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.text.WordUtils;
import svenhjol.charm.charmony.Resolve;
import svenhjol.strange.feature.travel_journals.TravelJournals;
import svenhjol.strange.feature.travel_journals.client.Resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class Helpers {
    /**
     * Fetch the most readily available travel journal held by the player.
     * The order that is checked is:
     * - mainhand
     * - offhand
     * - inventory slots starting from 0
     */
    public static ItemStack tryGetTravelJournal(Player player) {
        var items = collectPotentialItems(player);

        for (var stack : items) {
            if (stack.is(journalItem())) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Fetch a specific travel journal from the player's inventory.
     */
    public static ItemStack tryGetTravelJournal(Player player, UUID journalId) {
        var items = collectPotentialItems(player);

        for (var stack : items) {
            if (stack.is(journalItem())) {
                if (JournalData.get(stack).id().equals(journalId)) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static List<ItemStack> collectPotentialItems(Player player) {
        var inventory = player.getInventory();
        List<ItemStack> items = new ArrayList<>();

        for (InteractionHand hand : InteractionHand.values()) {
            items.add(player.getItemInHand(hand));
        }

        items.addAll(inventory.items);
        return items;
    }

    @SuppressWarnings("deprecation")
    public static List<Component> wrap(String str) {
        var wrapped = WordUtils.wrap(str, 30);
        return Arrays.stream(wrapped.split("\n")).map(s -> (Component)Component.literal(s)).toList();
    }
    
    public static String biomeName(Player player) {
        return Component.translatable(biomeLocaleKey(player)).getString();
    }
    
    public static Component positionAsText(BlockPos pos) {
        return Component.translatable(Resources.XYZ_KEY, pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static Component coordinatesAsText(BlockPos pos) {
        return Component.translatable(Resources.COORDINATES, pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static Component dimensionAsText(ResourceKey<Level> dimension) {
        return Component.translatable(dimensionLocaleKey(dimension));
    }

    /**
     * Get a locale key for a dimension.
     */
    public static String dimensionLocaleKey(ResourceKey<Level> dimension) {
        var location = dimension.location();
        var namespace = location.getNamespace();
        var path = location.getPath();
        return "dimension." + namespace + "." + path;
    }

    /**
     * Get a locale key for the biome at the player's current position.
     */
    public static String biomeLocaleKey(Player player) {
        var registry = player.level().registryAccess();
        var biome = player.level().getBiome(player.blockPosition());
        var key = registry.registryOrThrow(Registries.BIOME).getKey(biome.value());

        if (key == null) {
            throw new RuntimeException("Cannot get player biome");
        }

        var namespace = key.getNamespace();
        var path = key.getPath();
        return "biome." + namespace + "." + path;
    }
    
    private static Item journalItem() {
        return Resolve.feature(TravelJournals.class).registers.travelJournalItem.get();
    }
}
