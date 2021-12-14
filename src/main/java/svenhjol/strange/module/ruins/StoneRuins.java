package svenhjol.strange.module.ruins;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.ruins.feature.StoneRuinFeature;
import svenhjol.strange.module.structures.Processors;
import svenhjol.strange.module.structures.processor.AnvilDamageProcessor;
import svenhjol.strange.module.structures.processor.StoneBricksDecayProcessor;

/**
 * With special thanks to TG.
 * @see {https://github.com/TelepathicGrunt/StructureTutorialMod/blob/bcef90ea39d3389ccbf2cf8b28a1a4eb2eb44f6a/src/main/java/com/telepathicgrunt/structure_tutorial/STConfiguredStructures.java#L15}
 * @see {https://github.com/TelepathicGrunt/StructureTutorialMod/blob/bcef90ea39d3389ccbf2cf8b28a1a4eb2eb44f6a/src/main/java/com/telepathicgrunt/structure_tutorial/structures/RunDownHouseStructure.java#L122}
 */
public class StoneRuins implements IRuinsTheme {
    public static final ResourceLocation STONE_RUIN_ID = new ResourceLocation(Strange.MOD_ID, "stone_ruin");

    public static StructureFeature<JigsawConfiguration> STONE_RUIN_FEATURE;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CONFIGURED_FEATURE;
    public static StructureProcessorList STONE_RUINS;

    public void register() {
        int size = Math.max(0, Math.min(10, Ruins.stoneRuinSize));

        STONE_RUIN_FEATURE = new StoneRuinFeature(
            JigsawConfiguration.CODEC,
            new ResourceLocation(Strange.MOD_ID, "ruins/stone_starts"),
            size,
            8,
            16
        );

        // use a dummy configuration as a placeholder. Starts and pools will be generated using json template_pools.
        CONFIGURED_FEATURE = STONE_RUIN_FEATURE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 0));

        // register the feature via Fabric API
        FabricStructureBuilder.create(STONE_RUIN_ID, STONE_RUIN_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .defaultConfig(32, 18, 1158102994)
            .register();

        // register the configured structure feature with minecraft
        CommonRegistry.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "stone_ruin"), CONFIGURED_FEATURE);

        STONE_RUINS = CommonRegistry.processorList(new ResourceLocation(Strange.MOD_ID, "stone_ruins"), ImmutableList.of(
            Processors.IGNORE,
            StoneBricksDecayProcessor.INSTANCE,
            AnvilDamageProcessor.INSTANCE
        ));
    }

    public void runWhenEnabled() {
        // TODO: register overworld specific loot

        for (String configCategory : Ruins.stoneRuinBiomeCategories) {
            Biome.BiomeCategory category = BiomeHelper.getBiomeCategoryByName(configCategory);
            if (category != null) {
                BiomeHelper.addStructureToBiomeCategory(CONFIGURED_FEATURE, category);
            }
        }

        // TODO: player state callback
    }
}
