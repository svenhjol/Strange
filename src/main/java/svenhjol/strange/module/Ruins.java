package svenhjol.strange.module;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.BiomeHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.structure.RuinFeature;
import svenhjol.strange.structure.RuinGenerator;
import svenhjol.strange.structure.ruin.BambiMountainsRuin;

import static svenhjol.charm.base.helper.StructureHelper.addToBiome;
import static svenhjol.charm.base.helper.StructureHelper.registerConfiguredFeature;
import static svenhjol.strange.structure.RuinGenerator.*;

@Module(mod = Strange.MOD_ID, description = "Underground ruins with different themes according to the biome.")
public class Ruins extends CharmModule {
    public static final Identifier RUIN_ID = new Identifier(Strange.MOD_ID, "ruin");

    public static StructureFeature<StructurePoolFeatureConfig> RUIN_FEATURE;

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
        registerRuins();
    }

    @Override
    public void init() {
        initRuins();
    }

    private void registerRuins() {
        RUIN_FEATURE = new RuinFeature(StructurePoolFeatureConfig.CODEC);

        // register structure so that `strange:ruin` is a valid location and gen spacing is correct
        FabricStructureBuilder.create(RUIN_ID, RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(24, 8, 12125)
            .register();

        // create configuredFeature objects for each jigsaw pool
        BADLANDS    = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> BADLANDS_POOL, ruinSize));
        DESERT      = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> DESERT_POOL, ruinSize));
        FOREST      = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FOREST_POOL, ruinSize));
        JUNGLE      = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> JUNGLE_POOL, ruinSize));
        MOUNTAINS   = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> MOUNTAINS_POOL, ruinSize));
        NETHER      = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> NETHER_POOL, ruinSize));
        PLAINS      = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> PLAINS_POOL, ruinSize));
        SAVANNA     = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SAVANNA_POOL, ruinSize));
        SNOWY       = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SNOWY_POOL, ruinSize));
        TAIGA       = RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> TAIGA_POOL, ruinSize));

        // register each configuredFeature with MC registry against the RUIN_STRUCTURE
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_badlands"), BADLANDS);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_desert"), DESERT);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_forest"), FOREST);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_jungle"), JUNGLE);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_mountains"), MOUNTAINS);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_nether"), NETHER);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_plains"), PLAINS);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_savanna"), SAVANNA);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_snowy"), SNOWY);
        registerConfiguredFeature(new Identifier(Strange.MOD_ID, "ruin_taiga"), TAIGA);
    }

    private void initRuins() {
        // register all custom ruins here
        RuinGenerator.MOUNTAINS_RUINS.add(new BambiMountainsRuin());
//        RuinGenerator.FOREST_RUINS.add(new ForestRuin());
//        RuinGenerator.PLAINS_RUINS.add(new PlainsRuin());

        // builds and registers all custom ruins into pools
        RuinGenerator.init();

        // add registered ruin pools to biomes
        if (!RuinGenerator.BADLANDS_RUINS.isEmpty()) addToBiome(BiomeHelper.BADLANDS, BADLANDS);
        if (!RuinGenerator.DESERT_RUINS.isEmpty()) addToBiome(BiomeHelper.DESERT, DESERT);
        if (!RuinGenerator.FOREST_RUINS.isEmpty()) addToBiome(BiomeHelper.FOREST, FOREST);
        if (!RuinGenerator.JUNGLE_RUINS.isEmpty()) addToBiome(BiomeHelper.JUNGLE, JUNGLE);
        if (!RuinGenerator.MOUNTAINS_RUINS.isEmpty()) addToBiome(BiomeHelper.MOUNTAINS, MOUNTAINS);
        if (!RuinGenerator.NETHER_RUINS.isEmpty()) addToBiome(BiomeHelper.NETHER, NETHER);
        if (!RuinGenerator.PLAINS_RUINS.isEmpty()) addToBiome(BiomeHelper.PLAINS, PLAINS);
        if (!RuinGenerator.SAVANNA_RUINS.isEmpty()) addToBiome(BiomeHelper.SAVANNA, SAVANNA);
        if (!RuinGenerator.SNOWY_RUINS.isEmpty()) addToBiome(BiomeHelper.SNOWY, SNOWY);
        if (!RuinGenerator.TAIGA_RUINS.isEmpty()) addToBiome(BiomeHelper.TAIGA, TAIGA);
    }
}
