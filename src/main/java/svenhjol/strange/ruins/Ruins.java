package svenhjol.strange.ruins;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.BiomeHelper;
import svenhjol.charm.base.helper.DecorationHelper;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.ruins.builds.Roguelike;

import static svenhjol.charm.base.handler.RegistryHandler.configuredFeature;
import static svenhjol.charm.base.helper.StructureHelper.addToBiome;
import static svenhjol.strange.ruins.RuinGenerator.*;

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
    public static int configRuinSize = 7;

    @Override
    public void register() {
        RUIN_FEATURE = new RuinFeature(StructurePoolFeatureConfig.CODEC);

        // register structure so that `strange:ruin` is a valid location and gen spacing is correct
        FabricStructureBuilder.create(RUIN_ID, RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(24, 8, 12125)
            .register();

        int ruinSize = Math.max(0, Math.min(7, configRuinSize));

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
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_badlands"), BADLANDS);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_desert"), DESERT);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_forest"), FOREST);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_jungle"), JUNGLE);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_mountains"), MOUNTAINS);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_nether"), NETHER);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_plains"), PLAINS);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_savanna"), SAVANNA);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_snowy"), SNOWY);
        configuredFeature(new Identifier(Strange.MOD_ID, "ruin_taiga"), TAIGA);
    }

    @Override
    public void init() {
        // register rare ruin loot table
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUIN_RARE);
        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.RUIN_RARE);

        // register all custom ruins here
//        PLAINS_RUINS.add(new TestRuin());
//        MOUNTAINS_RUINS.add(new Vaults());
//        MOUNTAINS_RUINS.add(new BambiMountainsRuin());
//        FOREST_RUINS.add(new ForestRuin());
//            PLAINS_RUINS.add(new Castle());
        PLAINS_RUINS.add(new Roguelike());

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
