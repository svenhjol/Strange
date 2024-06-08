package svenhjol.strange.feature.travel_journals.common;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.charmony.Resolve;
import svenhjol.strange.feature.travel_journals.TravelJournals;

import java.util.ArrayList;
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
    
    private static Item journalItem() {
        return Resolve.feature(TravelJournals.class).registers.item.get();
    }
}
