package svenhjol.strange.module.discoveries;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runes.RuneHelper;
import svenhjol.strange.module.runes.Runes;
import svenhjol.strange.module.runestones.Runestones;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class DiscoveryHelper {
    /**
     * Create a new discovery for the provided location and difficulty.
     * If the location is the spawn point then an existing discovery will try to be returned.
     */
    @Nullable
    public static Discovery getOrCreate(float difficulty, ResourceLocation dimension, BlockPos pos, Random random, @Nullable ResourceLocation location, @Nullable UUID discoverer) {
        var discoveries = Discoveries.getDiscoveries().orElse(null);
        if (discoveries == null) return null;

        if (location != null && location.equals(Runestones.SPAWN)) {

            // Try and re-use an existing spawn point discovery.
            var first = discoveries.all().stream().filter(d -> d.getLocation().equals(Runestones.SPAWN)).findFirst();
            if (first.isPresent()) {
                return first.get();
            }

            // If there isn't a spawn point discovery then generate one now.
            var runes = RuneHelper.uniqueRunes(discoveries.branch, random, difficulty, 1, 1);
            var discovery = new Discovery(runes, Runestones.SPAWN);

            discoveries.add(discovery);
            LogHelper.debug(Strange.MOD_ID, DiscoveryHelper.class, "Registered spawn point discovery `" + discovery.getRunes() + "`.");
            return discovery;

        } else {

            // Chance to re-use an existing discovery.
            if (random.nextDouble() < Runestones.reuseChance) {
                List<Discovery> existing = discoveries.all().stream().filter(d -> Math.abs(d.getDifficulty() - difficulty) < 0.1F).collect(Collectors.toList());
                if (!existing.isEmpty()) {
                    Collections.shuffle(existing, random);
                    var discovery = existing.get(0);

                    LogHelper.debug(Strange.MOD_ID, DiscoveryHelper.class, "Reusing discovery `" + discovery.getRunes() + " : " + discovery.getLocation() + "`.");
                    return discovery;
                }
            }

            if (location == null) {
                // Generate a destination from the difficulty.  If it's still null then bail.
                location = getDestination(dimension, random, difficulty);
                if (location == null) return null;
            }

        }

        // Generate a discovery for the calculated location and provided difficulty.
        var runes = RuneHelper.uniqueRunes(discoveries.branch, random, difficulty, Runes.MIN_PHRASE_LENGTH, Runes.MAX_PHRASE_LENGTH);
        var discovery = new Discovery(runes, location);

        discovery.setPos(pos);
        discovery.setDifficulty(difficulty);
        discovery.setDimension(dimension);

        if (discoverer != null) {
            discovery.setPlayer(discoverer);
        }

        LogHelper.debug(Strange.MOD_ID, DiscoveryHelper.class, "Registered discovery `" + discovery.getRunes() + " : " + discovery.getLocation() + "`.");
        return discoveries.add(discovery);
    }

    /**
     * Generate a destination using the provided difficulty.
     */
    @Nullable
    public static ResourceLocation getDestination(ResourceLocation dimension, Random random, float difficulty) {
        if (!Runestones.DESTINATIONS.containsKey(dimension)) {

            // If the master destinations map doesn't contain any destinations for this dimension then we have to give up here.
            LogHelper.debug(Strange.MOD_ID, DiscoveryHelper.class, "No destinations are available for dimension `" + dimension + "`, giving up.");
            return null;

        }

        var destinations = new ArrayList<>(Runestones.DESTINATIONS.get(dimension));
        var index = Math.min(destinations.size() - 1, (int) Math.ceil(destinations.size() * difficulty));

        // We give some additional weighting to the first destination in the list.
        if (index == 1 && random.nextBoolean()) {
            index = 0;
        }

        return destinations.get(index);
    }
}
