package svenhjol.strange.module;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.BiomeHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.ruin.BambiMountainsRuin;
import svenhjol.strange.ruin.ForestRuin;
import svenhjol.strange.structure.RuinFeature;
import svenhjol.strange.structure.RuinGenerator;

import java.util.List;

import static svenhjol.strange.structure.RuinGenerator.*;

@Module(description = "Underground ruins with different themes according to the biome.")
public class Ruins extends MesonModule {
    public static final Identifier STRUCTURE_ID = new Identifier(Strange.MOD_ID, "ruin");

    public static StructureFeature<StructurePoolFeatureConfig> STRUCTURE;

    public static ConfiguredStructureFeature<?, ?> BADLANDS;
    public static ConfiguredStructureFeature<?, ?> DESERT;
    public static ConfiguredStructureFeature<?, ?> FOREST;
    public static ConfiguredStructureFeature<?, ?> JUNGLE;
    public static ConfiguredStructureFeature<?, ?> MOUNTAINS;
    public static ConfiguredStructureFeature<?, ?> NETHER;
    public static ConfiguredStructureFeature<?, ?> PLAINS;
    public static ConfiguredStructureFeature<?, ?> SAVANNA;
    public static ConfiguredStructureFeature<?, ?> SNOWY;
    public static ConfiguredStructureFeature<?, ?> TAIGA;

    @Config(name = "Ruin size", description = "Size of the generated ruins. For reference, villages are 6.")
    public static int ruinSize = 6;

    @Override
    public void register() {
        STRUCTURE = new RuinFeature(StructurePoolFeatureConfig.CODEC);

        // register structure so that `strange:ruin` is a valid location and gen spacing is correct
        FabricStructureBuilder.create(STRUCTURE_ID, STRUCTURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(24, 8, 12125)
            .register();

        // create configuredFeature objects for each jigsaw pool
        BADLANDS    = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> BADLANDS_POOL, ruinSize));
        DESERT      = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> DESERT_POOL, ruinSize));
        FOREST      = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> FOREST_POOL, ruinSize));
        JUNGLE      = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> JUNGLE_POOL, ruinSize));
        MOUNTAINS   = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> MOUNTAINS_POOL, ruinSize));
        NETHER      = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> NETHER_POOL, ruinSize));
        PLAINS      = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> PLAINS_POOL, ruinSize));
        SAVANNA     = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> SAVANNA_POOL, ruinSize));
        SNOWY       = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> SNOWY_POOL, ruinSize));
        TAIGA       = STRUCTURE.configure(new StructurePoolFeatureConfig(() -> TAIGA_POOL, ruinSize));

        // register each configuredFeature with MC registry against the RUIN_STRUCTURE
        registerConfiguredFeature("ruin_badlands", BADLANDS);
        registerConfiguredFeature("ruin_desert", DESERT);
        registerConfiguredFeature("ruin_forest", FOREST);
        registerConfiguredFeature("ruin_jungle", JUNGLE);
        registerConfiguredFeature("ruin_mountains", MOUNTAINS);
        registerConfiguredFeature("ruin_nether", NETHER);
        registerConfiguredFeature("ruin_plains", PLAINS);
        registerConfiguredFeature("ruin_savanna", SAVANNA);
        registerConfiguredFeature("ruin_snowy", SNOWY);
        registerConfiguredFeature("ruin_taiga", TAIGA);
    }

    @Override
    public void init() {
        // register all custom ruins here
        MOUNTAINS_RUINS.add(new BambiMountainsRuin());
        FOREST_RUINS.add(new ForestRuin());

        // builds and registers all custom ruins into pools
        RuinGenerator.init();

        // add registered ruin pools to biomes
        if (!BADLANDS_RUINS.isEmpty()) addToBiome(BiomeHelper.BADLANDS, BADLANDS);
        if (!DESERT_RUINS.isEmpty()) addToBiome(BiomeHelper.DESERT, DESERT);
        if (!FOREST_RUINS.isEmpty()) addToBiome(BiomeHelper.FOREST, FOREST);
        if (!JUNGLE_RUINS.isEmpty()) addToBiome(BiomeHelper.JUNGLE, JUNGLE);
        if (!MOUNTAINS_RUINS.isEmpty()) addToBiome(BiomeHelper.MOUNTAINS, MOUNTAINS);
        if (!NETHER_RUINS.isEmpty()) addToBiome(BiomeHelper.NETHER, NETHER);
        if (!PLAINS_RUINS.isEmpty()) addToBiome(BiomeHelper.PLAINS, PLAINS);
        if (!SAVANNA_RUINS.isEmpty()) addToBiome(BiomeHelper.SAVANNA, SAVANNA);
        if (!SNOWY_RUINS.isEmpty()) addToBiome(BiomeHelper.SNOWY, SNOWY);
        if (!TAIGA_RUINS.isEmpty()) addToBiome(BiomeHelper.TAIGA, TAIGA);
    }

    private void registerConfiguredFeature(String id, ConfiguredStructureFeature<?, ?> configuredFeature) {
        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, new Identifier(Strange.MOD_ID, id), configuredFeature);
    }

    private void addToBiome(List<String> biomeGroup, ConfiguredStructureFeature<?, ?> configuredFeature) {
        biomeGroup.forEach(id -> BuiltinRegistries.BIOME.getOrEmpty(new Identifier(id))
            .ifPresent(biome -> BiomeHelper.addStructureFeature(biome, configuredFeature)));
    }
}
