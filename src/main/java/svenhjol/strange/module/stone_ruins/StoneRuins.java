package svenhjol.strange.module.stone_ruins;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.AddRunestoneDestinationCallback;
import svenhjol.strange.init.StrangeEvents;
import svenhjol.strange.module.floating_islands_dimension.FloatingIslandsDimension;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * With special thanks to TG.
 * @link {https://github.com/TelepathicGrunt/StructureTutorialMod/blob/bcef90ea39d3389ccbf2cf8b28a1a4eb2eb44f6a/src/main/java/com/telepathicgrunt/structure_tutorial/STConfiguredStructures.java#L15}
 * @link {https://github.com/TelepathicGrunt/StructureTutorialMod/blob/bcef90ea39d3389ccbf2cf8b28a1a4eb2eb44f6a/src/main/java/com/telepathicgrunt/structure_tutorial/structures/RunDownHouseStructure.java#L122}
 */
@CommonModule(mod = Strange.MOD_ID)
public class StoneRuins extends CharmModule {
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(Strange.MOD_ID, "stone_ruin");
    public static final ResourceLocation STARTS = new ResourceLocation(Strange.MOD_ID, "stone_ruins/starts");

    public static StructureFeature<JigsawConfiguration> STONE_RUIN_FEATURE;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CONFIGURED_FEATURE;

    public static int ruinSize = 5;

    public static List<String> biomeCatgories = List.of(
        "plains", "desert", "mountains", "savanna", "forest", "icy", "mesa"
    );

    public static List<String> dimensionBlacklist = new ArrayList<>();

    @Override
    public void register() {
        int size = Math.max(0, Math.min(10, ruinSize));

        STONE_RUIN_FEATURE = new StoneRuinFeature(JigsawConfiguration.CODEC, STARTS, size, 8, 16);

        // use a dummy configuration as a placeholder. Starts and pools will be generated using json template_pools.
        CONFIGURED_FEATURE = STONE_RUIN_FEATURE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 0));

        // register the feature via Fabric API
        FabricStructureBuilder.create(STRUCTURE_ID, STONE_RUIN_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .defaultConfig(32, 24, 1158102994)
            .register();

        // register the configured structure feature with minecraft
        CommonRegistry.configuredStructureFeature(STRUCTURE_ID, CONFIGURED_FEATURE);
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(StrangeEvents.WORLD_LOAD_PHASE, this::handleWorldLoad);
        AddRunestoneDestinationCallback.EVENT.register(this::handleAddRunestoneDestination);

        // TODO: register overworld specific loot

        for (String configCategory : biomeCatgories) {
            Biome.BiomeCategory category = BiomeHelper.getBiomeCategoryByName(configCategory);
            if (category != null) {
                BiomeHelper.addStructureToBiomeCategory(CONFIGURED_FEATURE, category);
            }
        }

        // We don't want to try and generate these kinds of ruins in the Nether or End.
        // Force add them to the blacklist to prevent generation and adding of destinations to runestones.
        dimensionBlacklist.addAll(List.of(
            Level.NETHER.location().toString(),
            Level.END.location().toString(),
            FloatingIslandsDimension.ID.toString()
        ));
    }

    /**
     * Remove the ruins from structure generation for blacklisted dimensions.
     */
    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        if (dimensionBlacklist.contains(level.dimension().location().toString())) {
            WorldHelper.removeStructures(level, List.of(STONE_RUIN_FEATURE));
        }
    }

    /**
     * Add the ruins as a runestone destination to valid dimensions.
     */
    private void handleAddRunestoneDestination(Level level, LinkedList<ResourceLocation> destinations) {
        if (!dimensionBlacklist.contains(level.dimension().location().toString())) {
            if (!destinations.contains(STRUCTURE_ID)) {
                int size = destinations.size();
                int index = Math.max(0, Math.round(size * 0.85F));
                destinations.add(index, STRUCTURE_ID);
            }
        }
    }
}
