package svenhjol.strange.ruins;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.helper.DecorationHelper;
import svenhjol.charm.base.helper.LootHelper;
import svenhjol.charm.base.helper.PosHelper;
import svenhjol.charm.base.iface.Config;
import svenhjol.charm.base.iface.Module;
import svenhjol.charm.module.PlayerState;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;

import static net.minecraft.world.biome.Biome.Category;
import static svenhjol.charm.base.handler.RegistryHandler.configuredFeature;
import static svenhjol.charm.base.helper.BiomeHelper.addStructureFeatureToBiomes;

@Module(mod = Strange.MOD_ID, description = "Ruined structures with different themes according to the biome.")
public class Ruins extends CharmModule {
    public static final Identifier SURFACE_RUIN_ID = new Identifier(Strange.MOD_ID, "surface_ruin");
    public static final Identifier UNDERGROUND_RUIN_ID = new Identifier(Strange.MOD_ID, "underground_ruin");
    public static final Identifier FOUNDATION_RUIN_ID = new Identifier(Strange.MOD_ID, "foundation_ruin");

    public static StructureFeature<StructurePoolFeatureConfig> SURFACE_RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> UNDERGROUND_RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> FOUNDATION_RUIN_FEATURE;

    public static ConfiguredStructureFeature<?, ?> SURFACE_BADLANDS;
    public static ConfiguredStructureFeature<?, ?> SURFACE_DESERT;
    public static ConfiguredStructureFeature<?, ?> SURFACE_END;
    public static ConfiguredStructureFeature<?, ?> SURFACE_FOREST;
    public static ConfiguredStructureFeature<?, ?> SURFACE_JUNGLE;
    public static ConfiguredStructureFeature<?, ?> SURFACE_MOUNTAINS;
    public static ConfiguredStructureFeature<?, ?> SURFACE_OCEAN;
    public static ConfiguredStructureFeature<?, ?> SURFACE_PLAINS;
    public static ConfiguredStructureFeature<?, ?> SURFACE_SAVANNA;
    public static ConfiguredStructureFeature<?, ?> SURFACE_SNOWY;
    public static ConfiguredStructureFeature<?, ?> SURFACE_TAIGA;

    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_BADLANDS;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_DESERT;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_FOREST;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_JUNGLE;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_MOUNTAINS;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_NETHER;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_PLAINS;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_SAVANNA;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_SNOWY;
    public static ConfiguredStructureFeature<?, ?> UNDERGROUND_TAIGA;

    public static ConfiguredStructureFeature<?, ?> FOUNDATION_BADLANDS;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_DESERT;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_FOREST;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_JUNGLE;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_MOUNTAINS;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_NETHER;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_PLAINS;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_SAVANNA;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_SNOWY;
    public static ConfiguredStructureFeature<?, ?> FOUNDATION_TAIGA;

    @Config(name = "Surface ruin size", description = "Size of the generated surface ruins.")
    public static int configSurfaceRuinSize = 4;

    @Config(name = "Underground ruin size", description = "Size of the generated underground ruins.")
    public static int configUndergroundRuinSize = 7;

    @Config(name = "Foundation ruin size", description = "Size of the generated foundation ruins.")
    public static int configFoundationRuinSize = 5;

    @Override
    public void register() {
        SURFACE_RUIN_FEATURE = new SurfaceRuinFeature(StructurePoolFeatureConfig.CODEC);
        UNDERGROUND_RUIN_FEATURE = new UndergroundRuinFeature(StructurePoolFeatureConfig.CODEC);
        FOUNDATION_RUIN_FEATURE = new FoundationRuinFeature(StructurePoolFeatureConfig.CODEC);

        FabricStructureBuilder.create(SURFACE_RUIN_ID, SURFACE_RUIN_FEATURE)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(24, 8, 5215435)
            .register();

        FabricStructureBuilder.create(UNDERGROUND_RUIN_ID, UNDERGROUND_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(18, 12, 25151)
            .register();

        FabricStructureBuilder.create(FOUNDATION_RUIN_ID, FOUNDATION_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(24, 8, 25151)
            .register();

        int surfaceRuinSize = Math.max(0, Math.min(7, configSurfaceRuinSize));
        int undergroundRuinSize = Math.max(0, Math.min(7, configUndergroundRuinSize));
        int foundationRuinSize = Math.max(0, Math.min(7, configFoundationRuinSize));

        SURFACE_BADLANDS    = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.BADLANDS_POOL, surfaceRuinSize));
        SURFACE_DESERT      = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.DESERT_POOL, surfaceRuinSize));
        SURFACE_END         = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.END_POOL, surfaceRuinSize));
        SURFACE_FOREST      = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.FOREST_POOL, surfaceRuinSize));
        SURFACE_JUNGLE      = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.JUNGLE_POOL, surfaceRuinSize));
        SURFACE_MOUNTAINS   = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.MOUNTAINS_POOL, surfaceRuinSize));
        SURFACE_OCEAN       = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.OCEAN_POOL, surfaceRuinSize));
        SURFACE_PLAINS      = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.PLAINS_POOL, surfaceRuinSize));
        SURFACE_SAVANNA     = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.SAVANNA_POOL, surfaceRuinSize));
        SURFACE_SNOWY       = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.SNOWY_POOL, surfaceRuinSize));
        SURFACE_TAIGA       = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.TAIGA_POOL, surfaceRuinSize));

        UNDERGROUND_BADLANDS    = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.BADLANDS_POOL, undergroundRuinSize));
        UNDERGROUND_DESERT      = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.DESERT_POOL, undergroundRuinSize));
        UNDERGROUND_FOREST      = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.FOREST_POOL, undergroundRuinSize));
        UNDERGROUND_JUNGLE      = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.JUNGLE_POOL, undergroundRuinSize));
        UNDERGROUND_MOUNTAINS   = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.MOUNTAINS_POOL, undergroundRuinSize));
        UNDERGROUND_NETHER      = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.NETHER_POOL, undergroundRuinSize));
        UNDERGROUND_PLAINS      = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.PLAINS_POOL, undergroundRuinSize));
        UNDERGROUND_SAVANNA     = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.SAVANNA_POOL, undergroundRuinSize));
        UNDERGROUND_SNOWY       = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.SNOWY_POOL, undergroundRuinSize));
        UNDERGROUND_TAIGA       = UNDERGROUND_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> UndergroundRuinGenerator.TAIGA_POOL, undergroundRuinSize));

        FOUNDATION_BADLANDS    = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.BADLANDS_POOL, foundationRuinSize));
        FOUNDATION_DESERT      = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.DESERT_POOL, foundationRuinSize));
        FOUNDATION_FOREST      = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.FOREST_POOL, foundationRuinSize));
        FOUNDATION_JUNGLE      = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.JUNGLE_POOL, foundationRuinSize));
        FOUNDATION_MOUNTAINS   = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.MOUNTAINS_POOL, foundationRuinSize));
        FOUNDATION_NETHER      = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.NETHER_POOL, foundationRuinSize));
        FOUNDATION_PLAINS      = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.PLAINS_POOL, foundationRuinSize));
        FOUNDATION_SAVANNA     = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.SAVANNA_POOL, foundationRuinSize));
        FOUNDATION_SNOWY       = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.SNOWY_POOL, foundationRuinSize));
        FOUNDATION_TAIGA       = FOUNDATION_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> FoundationRuinGenerator.TAIGA_POOL, foundationRuinSize));

        // register each configuredFeature with MC registry against the RUIN_STRUCTURE
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_badlands"), SURFACE_BADLANDS);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_desert"), SURFACE_DESERT);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_end"), SURFACE_END);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_forest"), SURFACE_FOREST);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_jungle"), SURFACE_JUNGLE);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_mountains"), SURFACE_MOUNTAINS);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_ocean"), SURFACE_OCEAN);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_plains"), SURFACE_PLAINS);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_savanna"), SURFACE_SAVANNA);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_snowy"), SURFACE_SNOWY);
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin_taiga"), SURFACE_TAIGA);

        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_badlands"), UNDERGROUND_BADLANDS);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_desert"), UNDERGROUND_DESERT);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_forest"), UNDERGROUND_FOREST);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_jungle"), UNDERGROUND_JUNGLE);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_mountains"), UNDERGROUND_MOUNTAINS);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_nether"), UNDERGROUND_NETHER);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_plains"), UNDERGROUND_PLAINS);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_savanna"), UNDERGROUND_SAVANNA);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_snowy"), UNDERGROUND_SNOWY);
        configuredFeature(new Identifier(Strange.MOD_ID, "underground_ruin_taiga"), UNDERGROUND_TAIGA);

        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_badlands"), FOUNDATION_BADLANDS);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_desert"), FOUNDATION_DESERT);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_forest"), FOUNDATION_FOREST);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_jungle"), FOUNDATION_JUNGLE);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_mountains"), FOUNDATION_MOUNTAINS);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_nether"), FOUNDATION_NETHER);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_plains"), FOUNDATION_PLAINS);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_savanna"), FOUNDATION_SAVANNA);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_snowy"), FOUNDATION_SNOWY);
        configuredFeature(new Identifier(Strange.MOD_ID, "foundation_ruin_taiga"), FOUNDATION_TAIGA);
    }

    @Override
    public void init() {
        // register rare ruin loot table
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUIN_RARE);
        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.RUIN_RARE);

        RuinBuilds.init();

        // builds and registers all custom ruins into pools
        SurfaceRuinGenerator.init();
        UndergroundRuinGenerator.init();
        FoundationRuinGenerator.init();

        // add registered ruin pools to biomes
        if (!SurfaceRuinGenerator.BADLANDS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.MESA, SURFACE_BADLANDS);
        if (!SurfaceRuinGenerator.DESERT_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.DESERT, SURFACE_DESERT);
        if (!SurfaceRuinGenerator.END_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.THEEND, SURFACE_END);
        if (!SurfaceRuinGenerator.FOREST_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.FOREST, SURFACE_FOREST);
        if (!SurfaceRuinGenerator.JUNGLE_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.JUNGLE, SURFACE_JUNGLE);
        if (!SurfaceRuinGenerator.MOUNTAINS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.EXTREME_HILLS, SURFACE_MOUNTAINS);
        if (!SurfaceRuinGenerator.OCEAN_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.OCEAN, SURFACE_OCEAN);
        if (!SurfaceRuinGenerator.PLAINS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.PLAINS, SURFACE_PLAINS);
        if (!SurfaceRuinGenerator.SAVANNA_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.SAVANNA, SURFACE_SAVANNA);
        if (!SurfaceRuinGenerator.SNOWY_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.ICY, SURFACE_SNOWY);
        if (!SurfaceRuinGenerator.TAIGA_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.TAIGA, SURFACE_TAIGA);

        if (!UndergroundRuinGenerator.BADLANDS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.MESA, UNDERGROUND_BADLANDS);
        if (!UndergroundRuinGenerator.DESERT_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.DESERT, UNDERGROUND_DESERT);
        if (!UndergroundRuinGenerator.FOREST_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.FOREST, UNDERGROUND_FOREST);
        if (!UndergroundRuinGenerator.JUNGLE_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.JUNGLE, UNDERGROUND_JUNGLE);
        if (!UndergroundRuinGenerator.MOUNTAINS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.EXTREME_HILLS, UNDERGROUND_MOUNTAINS);
        if (!UndergroundRuinGenerator.NETHER_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.NETHER, UNDERGROUND_MOUNTAINS);
        if (!UndergroundRuinGenerator.PLAINS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.PLAINS, UNDERGROUND_PLAINS);
        if (!UndergroundRuinGenerator.SAVANNA_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.SAVANNA, UNDERGROUND_SAVANNA);
        if (!UndergroundRuinGenerator.SNOWY_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.ICY, UNDERGROUND_SNOWY);
        if (!UndergroundRuinGenerator.TAIGA_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.TAIGA, UNDERGROUND_TAIGA);

        if (!FoundationRuinGenerator.BADLANDS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.MESA, FOUNDATION_BADLANDS);
        if (!FoundationRuinGenerator.DESERT_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.DESERT, FOUNDATION_DESERT);
        if (!FoundationRuinGenerator.FOREST_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.FOREST, FOUNDATION_FOREST);
        if (!FoundationRuinGenerator.JUNGLE_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.JUNGLE, FOUNDATION_JUNGLE);
        if (!FoundationRuinGenerator.MOUNTAINS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.EXTREME_HILLS, FOUNDATION_MOUNTAINS);
        if (!FoundationRuinGenerator.NETHER_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.NETHER, FOUNDATION_MOUNTAINS);
        if (!FoundationRuinGenerator.PLAINS_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.PLAINS, FOUNDATION_PLAINS);
        if (!FoundationRuinGenerator.SAVANNA_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.SAVANNA, FOUNDATION_SAVANNA);
        if (!FoundationRuinGenerator.SNOWY_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.ICY, FOUNDATION_SNOWY);
        if (!FoundationRuinGenerator.TAIGA_RUINS.isEmpty()) addStructureFeatureToBiomes(Category.TAIGA, FOUNDATION_TAIGA);

        // add player location callback
        PlayerState.listeners.add((player, tag) -> {
            if (player != null && player.world != null && !player.world.isClient) {
                ServerWorld serverWorld = (ServerWorld) player.world;
                BlockPos playerPos = player.getBlockPos();
                boolean isInRuin = PosHelper.isInsideStructure(serverWorld, playerPos, SURFACE_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, UNDERGROUND_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, FOUNDATION_RUIN_FEATURE);

                tag.putBoolean("ruin", isInRuin);
            }
        });
    }
}
