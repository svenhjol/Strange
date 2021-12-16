package svenhjol.strange.module.ruins;

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
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.AddRunestoneDestinationCallback;
import svenhjol.strange.init.StrangeEvents;

import java.util.LinkedList;
import java.util.List;

/**
 * With special thanks to TG.
 * @see {https://github.com/TelepathicGrunt/StructureTutorialMod/blob/bcef90ea39d3389ccbf2cf8b28a1a4eb2eb44f6a/src/main/java/com/telepathicgrunt/structure_tutorial/STConfiguredStructures.java#L15}
 * @see {https://github.com/TelepathicGrunt/StructureTutorialMod/blob/bcef90ea39d3389ccbf2cf8b28a1a4eb2eb44f6a/src/main/java/com/telepathicgrunt/structure_tutorial/structures/RunDownHouseStructure.java#L122}
 */
public class StoneRuins implements IRuinType {
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(Strange.MOD_ID, "stone_ruin");
    public static final ResourceLocation STARTS = new ResourceLocation(Strange.MOD_ID, "ruins/stone_starts");

    public static StructureFeature<JigsawConfiguration> STONE_RUIN_FEATURE;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CONFIGURED_FEATURE;

    public void register() {
        int size = Math.max(0, Math.min(10, Ruins.stoneRuinSize));

        STONE_RUIN_FEATURE = new StoneRuinFeature(JigsawConfiguration.CODEC, STARTS, size, 8, 16);

        // use a dummy configuration as a placeholder. Starts and pools will be generated using json template_pools.
        CONFIGURED_FEATURE = STONE_RUIN_FEATURE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 0));

        // register the feature via Fabric API
        FabricStructureBuilder.create(STRUCTURE_ID, STONE_RUIN_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .defaultConfig(32, 18, 1158102994)
            .register();

        // register the configured structure feature with minecraft
        CommonRegistry.configuredStructureFeature(STRUCTURE_ID, CONFIGURED_FEATURE);
    }

    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(StrangeEvents.WORLD_LOAD_PHASE, this::handleWorldLoad);
        AddRunestoneDestinationCallback.EVENT.register(this::handleAddRunestoneDestination);

        // TODO: register overworld specific loot

        for (String configCategory : Ruins.stoneRuinBiomeCategories) {
            Biome.BiomeCategory category = BiomeHelper.getBiomeCategoryByName(configCategory);
            if (category != null) {
                BiomeHelper.addStructureToBiomeCategory(CONFIGURED_FEATURE, category);
            }
        }

        // TODO: player state callback
    }

    /**
     * Remove the ruins from structure generation for blacklisted dimensions.
     */
    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        if (Ruins.stoneRuinDimensionBlacklist.contains(level.dimension().location().toString())) {
            WorldHelper.removeStructures(level, List.of(STONE_RUIN_FEATURE));
        }
    }

    /**
     * Add the ruins as a runestone destination to valid dimensions.
     */
    private void handleAddRunestoneDestination(Level level, LinkedList<ResourceLocation> destinations) {
        if (!Ruins.stoneRuinDimensionBlacklist.contains(level.dimension().location().toString())) {
            if (!destinations.contains(STRUCTURE_ID)) {
                int size = destinations.size();
                int index = Math.max(0, Math.round(size * 0.85F));
                destinations.add(index, STRUCTURE_ID);
            }
        }
    }
}
