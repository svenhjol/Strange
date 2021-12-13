package svenhjol.strange.module.ruins;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.ruins.build.TestRuin;
import svenhjol.strange.module.ruins.feature.OverworldRuinFeature;
import svenhjol.strange.module.ruins.generator.OverworldRuinGenerator;
import svenhjol.strange.module.structures.BaseStructure;

import java.util.Arrays;
import java.util.List;

public class OverworldRuins implements IRuinsTheme {
    public static final ResourceLocation OVERWORLD_RUIN_ID = new ResourceLocation(Strange.MOD_ID, "overworld_ruin");

    public static StructureFeature<JigsawConfiguration> OVERWORLD_RUIN_FEATURE;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CONFIGURED_FEATURE;

    public void register() {
        int size = Math.max(0, Math.min(7, Ruins.overworldRuinSize));

        OVERWORLD_RUIN_FEATURE = new OverworldRuinFeature(JigsawConfiguration.CODEC);
        CONFIGURED_FEATURE = OVERWORLD_RUIN_FEATURE.configured(new JigsawConfiguration(() -> OverworldRuinGenerator.POOL, size));

        FabricStructureBuilder.create(OVERWORLD_RUIN_ID, OVERWORLD_RUIN_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .defaultConfig(32, 18, 1158102994)
            .register();

        // register the configured structure feature with minecraft
        CommonRegistry.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "overworld_ruin"), CONFIGURED_FEATURE);

        // add ruin builds to the overworld ruin generator
        OverworldRuinGenerator.RUINS.addAll(getBuilds());

        // initialize the generator
        OverworldRuinGenerator.init();
    }

    public void runWhenEnabled() {
        if (OverworldRuinGenerator.RUINS.isEmpty()) return;

        // TODO: register overworld specific loot

        for (String configCategory : Ruins.overworldBiomeCategories) {
            Biome.BiomeCategory category = BiomeHelper.getBiomeCategoryByName(configCategory);
            if (category != null) {
                BiomeHelper.addStructureToBiomeCategory(CONFIGURED_FEATURE, category);
            }
        }

        // TODO: player state callback
    }

    @Override
    public List<BaseStructure> getBuilds() {
        return Arrays.asList(new TestRuin());
    }
}
