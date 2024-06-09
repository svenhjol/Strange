package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.core.BlockPos;
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

import java.util.Arrays;
import java.util.LinkedList;
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

    /**
     * Gets an ordered list of items from the player, starting with hands and then inventory.
     */
    public static List<ItemStack> collectPotentialItems(Player player) {
        var inventory = player.getInventory();
        List<ItemStack> items = new LinkedList<>();

        for (InteractionHand hand : InteractionHand.values()) {
            items.add(player.getItemInHand(hand));
        }

        items.addAll(inventory.items);
        return items;
    }

    /**
     * Wrap string at a sensible line length and converts into a list of components.
     * This uses an old version of WordUtils which may be problematic?
     */
    @SuppressWarnings("deprecation")
    public static List<Component> wrap(String str) {
        var wrapped = WordUtils.wrap(str, 30);
        return Arrays.stream(wrapped.split("\n")).map(s -> (Component) Component.literal(s)).toList();
    }

    /**
     * Get formatted text component of the given blockpos.
     */
    public static Component positionAsText(BlockPos pos) {
        return Component.translatable(Resources.XYZ_KEY, pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Get formatted text component of the given dimension.
     */
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
     * Internal helper to get the registered travel journal item.
     */
    private static Item journalItem() {
        return Resolve.feature(TravelJournals.class).registers.travelJournalItem.get();
    }
}
