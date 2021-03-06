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
import static svenhjol.charm.base.helper.BiomeHelper.addStructureToBiomeCategories;

@Module(mod = Strange.MOD_ID, description = "Ruined structures.")
public class Ruins extends CharmModule {
    public static final Identifier SURFACE_RUIN_ID = new Identifier(Strange.MOD_ID, "surface_ruin");
    public static final Identifier CAVE_RUIN_ID = new Identifier(Strange.MOD_ID, "cave_ruin");
    public static final Identifier DEEP_RUIN_ID = new Identifier(Strange.MOD_ID, "deep_ruin");

    public static StructureFeature<StructurePoolFeatureConfig> SURFACE_RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> CAVE_RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> DEEP_RUIN_FEATURE;

    public static ConfiguredStructureFeature<?, ?> SURFACE_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<?, ?> CAVE_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<?, ?> DEEP_RUIN_CONFIGURED;

    @Config(name = "Surface ruin size", description = "Size of the generated surface ruins.")
    public static int configSurfaceRuinSize = 4;

    @Config(name = "Cave ruin size", description = "Size of the generated cave ruins.")
    public static int configCaveRuinSize = 7;

    @Config(name = "Deep ruin size", description = "Size of the generated deep ruins.")
    public static int configDeepRuinSize = 5;

    @Override
    public void register() {
        SURFACE_RUIN_FEATURE = new SurfaceRuinFeature(StructurePoolFeatureConfig.CODEC);
        CAVE_RUIN_FEATURE = new CaveRuinFeature(StructurePoolFeatureConfig.CODEC);
        DEEP_RUIN_FEATURE = new DeepRuinFeature(StructurePoolFeatureConfig.CODEC);

        FabricStructureBuilder.create(SURFACE_RUIN_ID, SURFACE_RUIN_FEATURE)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(24, 8, 5215435)
            .register();

        FabricStructureBuilder.create(CAVE_RUIN_ID, CAVE_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(18, 12, 25151)
            .register();

        FabricStructureBuilder.create(DEEP_RUIN_ID, DEEP_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(24, 8, 25151)
            .register();

        int surfaceRuinSize = Math.max(0, Math.min(7, configSurfaceRuinSize));
        int caveRuinSize = Math.max(0, Math.min(7, configCaveRuinSize));
        int deepRuinSize = Math.max(0, Math.min(7, configDeepRuinSize));

        SURFACE_RUIN_CONFIGURED = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.POOL, surfaceRuinSize));
        CAVE_RUIN_CONFIGURED = CAVE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> CaveRuinGenerator.POOL, caveRuinSize));
        DEEP_RUIN_CONFIGURED = DEEP_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> DeepRuinGenerator.POOL, deepRuinSize));

        // register each configuredFeature with MC registry against the RUIN_STRUCTURE
        configuredFeature(new Identifier(Strange.MOD_ID, "surface_ruin"), SURFACE_RUIN_CONFIGURED);
        configuredFeature(new Identifier(Strange.MOD_ID, "cave_ruin"), CAVE_RUIN_CONFIGURED);
        configuredFeature(new Identifier(Strange.MOD_ID, "deep_ruin"), DEEP_RUIN_CONFIGURED);
    }

    @Override
    public void init() {
        // register ruin loot tables
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUINS_COMMON);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUINS_UNCOMMON);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUINS_RARE);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.RUINS_EPIC);

        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.RUINS_RARE);
        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.RUINS_EPIC);

        RuinBuilds.init();

        // builds and registers all custom ruins into pools
        SurfaceRuinGenerator.init();
        CaveRuinGenerator.init();
        DeepRuinGenerator.init();

        // add registered ruin pools to biomes
        if (!SurfaceRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(SURFACE_RUIN_CONFIGURED, Category.PLAINS);
        if (!CaveRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(CAVE_RUIN_CONFIGURED, Category.PLAINS);
        if (!DeepRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(DEEP_RUIN_CONFIGURED, Category.PLAINS);

        // add player location callback
        PlayerState.listeners.add((player, tag) -> {
            if (player != null && player.world != null && !player.world.isClient) {
                ServerWorld serverWorld = (ServerWorld) player.world;
                BlockPos playerPos = player.getBlockPos();
                boolean isInRuin = PosHelper.isInsideStructure(serverWorld, playerPos, SURFACE_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, CAVE_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, DEEP_RUIN_FEATURE);

                tag.putBoolean("ruin", isInRuin);
            }
        });
    }
}
