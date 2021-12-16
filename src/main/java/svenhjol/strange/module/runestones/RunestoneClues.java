package svenhjol.strange.module.runestones;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import svenhjol.charm.helper.WorldHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RunestoneClues {
    public static void init(MinecraftServer server) {
        Runestones.CLUES = new HashMap<>();

        Runestones.CLUES.put(Runestones.SPAWN, getCluesForLocation(Runestones.SPAWN));

        Runestones.DESTINATIONS.forEach((dimension, locations) -> {
            for (ResourceLocation location : locations) {
                Runestones.CLUES.put(location, getCluesForLocation(location));
            }
        });
    }

    private static List<String> getCluesForLocation(ResourceLocation res) {
        List<String> clues = new ArrayList<>();
        String path = res.getPath();

        if (WorldHelper.isBiome(res)) {
            clues.add("biome");
        }

        if (WorldHelper.isStructure(res)) {
            clues.add("structure");
        }

        if (List.of(
            "spawn_point",
            "village",
            "buried_treasure",
            "igloo"
        ).contains(path)) clues.add("safe");

        if (List.of(
            "stronghold",
            "mineshaft",
            "buried_treasure",
            "dripstone_caves",
            "lush_caves"
        ).contains(path)) clues.add("underground");

        if (List.of(
            "ocean_ruin",
            "monument",
            "warm_ocean",
            "deep_warm_ocean"
        ).contains(path)) clues.add("underwater");

        if (List.of(
            "village",
            "endcity",
            "jungle_pyramid",
            "desert_pyramid",
            "mansion",
            "igloo",
            "swamp_hut",
            "pillager_outpost"
        ).contains(path)) clues.add("surface");

        if (List.of(
            "stone_circle",
            "stronghold",
            "ocean_ruin"
        ).contains(path)) clues.add("ancient");

        if (List.of(
            "pillager_outpost",
            "mansion",
            "fortress",
            "basalt_deltas"
        ).contains(path)) clues.add("dangerous");

        if (List.of(
            "desert_pyramid",
            "jungle_pyramid",
            "shipwreck",
            "monument",
            "mansion",
            "ocean_ruin",
            "fortress",
            "endcity",
            "ruined_portal",
            "buried_treasure",
            "bastion_remnant"
        ).contains(path)) clues.add("lucrative");

        return clues;
    }
}
