package svenhjol.strange.module;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.BiomeHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.structure.AncientRuinFeature;
import svenhjol.strange.structure.AncientRuinGenerator;
import svenhjol.strange.structure.RuinFeature;
import svenhjol.strange.structure.RuinGenerator;
import svenhjol.strange.structure.ancientruin.StoneRoomRuin;
import svenhjol.strange.structure.ruin.*;

import java.util.List;

import static svenhjol.strange.structure.RuinGenerator.*;

@Module(mod = Strange.MOD_ID, description = "Underground ruins with different themes according to the biome.")
public class Ruins extends CharmModule {
    public static final Identifier RUIN_ID = new Identifier(Strange.MOD_ID, "ruin");
    public static final Identifier ANCIENT_RUIN_ID = new Identifier(Strange.MOD_ID, "ancient_ruin");

    public static StructureFeature<StructurePoolFeatureConfig> RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> ANCIENT_RUIN_FEATURE;

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

    public static ConfiguredStructureFeature<?, ?> OVERWORLD;

    @Config(name = "Ruin size", description = "Size of the generated ruins. For reference, villages are 6.")
    public static int ruinSize = 6;

    public static int ancientRuinSize = 2;

    @Override
    public void register() {
        registerRuins();
        registerAncientRuins();
    }

    @Override
    public void init() {
        initRuins();
        initAncientRuins();
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

    private void registerAncientRuins() {
        ANCIENT_RUIN_FEATURE = new AncientRuinFeature(StructurePoolFeatureConfig.CODEC);

        FabricStructureBuilder.create(ANCIENT_RUIN_ID, ANCIENT_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(96, 16, 4231521)
            .register();

        // create configuredFeature objects for each jigsaw pool
        OVERWORLD = ANCIENT_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> AncientRuinGenerator.OVERWORLD_POOL, ancientRuinSize));

        // register each configuredFeature with MC registry against the ANCIENT_RUIN_STRUCTURE
        registerConfiguredFeature("ancient_ruin_overworld", OVERWORLD);
    }

    private void initRuins() {
        // register all custom ruins here
        RuinGenerator.MOUNTAINS_RUINS.add(new BambiMountainsRuin());
        RuinGenerator.FOREST_RUINS.add(new ForestRuin());
        RuinGenerator.PLAINS_RUINS.add(new PlainsRuin());

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

    private void initAncientRuins() {
        // register all custom overworld ancient ruins here
        AncientRuinGenerator.OVERWORLD_RUINS.add(new StoneRoomRuin());

        // builds and registers all custom ruins into pools
        AncientRuinGenerator.init();

        // TODO: method to add to all biomes
        if (!AncientRuinGenerator.OVERWORLD_RUINS.isEmpty()) {
            addToBiome(BiomeHelper.BADLANDS, OVERWORLD);
            addToBiome(BiomeHelper.DESERT, OVERWORLD);
            addToBiome(BiomeHelper.FOREST, OVERWORLD);
            addToBiome(BiomeHelper.JUNGLE, OVERWORLD);
            addToBiome(BiomeHelper.MOUNTAINS, OVERWORLD);
            addToBiome(BiomeHelper.NETHER, OVERWORLD);
            addToBiome(BiomeHelper.PLAINS, OVERWORLD);
            addToBiome(BiomeHelper.SAVANNA, OVERWORLD);
            addToBiome(BiomeHelper.SNOWY, OVERWORLD);
            addToBiome(BiomeHelper.TAIGA, OVERWORLD);
        }
    }

    private void registerConfiguredFeature(String id, ConfiguredStructureFeature<?, ?> configuredFeature) {
        BuiltinRegistries.add(BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, new Identifier(Strange.MOD_ID, id), configuredFeature);
    }

    private void addToBiome(List<String> biomeGroup, ConfiguredStructureFeature<?, ?> configuredFeature) {
        biomeGroup.forEach(id -> BuiltinRegistries.BIOME.getOrEmpty(new Identifier(id))
            .ifPresent(biome -> BiomeHelper.addStructureFeature(biome, configuredFeature)));
    }
}
