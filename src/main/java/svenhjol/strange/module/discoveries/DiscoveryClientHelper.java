package svenhjol.strange.module.discoveries;

import svenhjol.charm.helper.ClientHelper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DiscoveryClientHelper {
    /**
     * Get list of discoveries that the player has found, including
     * discoveries that do not belong to any player (e.g. spawn position).
     *
     * This list includes discoveries the player hasn't yet learned runes for.
     * Use JournalClientHelper#getFilteredDiscoveries for a restricted list.
     */
    public static List<Discovery> getPlayerDiscoveries() {
        var discoveries = DiscoveriesClient.getBranch().orElse(null);
        if (discoveries == null) return List.of();

        var player = ClientHelper.getPlayer().orElse(null);
        if (player == null) return List.of();

        var all = discoveries.all();

        return all.values().stream()
            .filter(d -> d.getPlayer() == null || d.getPlayer().equals(player.getUUID()))
            .sorted(Comparator.comparingLong(Discovery::getTime).reversed())
            .collect(Collectors.toList());
    }
}
