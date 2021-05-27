package svenhjol.strange.module;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeKeys;
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
import svenhjol.strange.StrangeLoot;
import svenhjol.strange.world.gen.*;
import svenhjol.strange.ruin.build.*;
import svenhjol.strange.world.gen.feature.*;

import static net.minecraft.world.biome.Biome.Category;
import static svenhjol.charm.base.handler.RegistryHandler.configuredStructureFeature;
import static svenhjol.charm.base.helper.BiomeHelper.addStructureToBiome;
import static svenhjol.charm.base.helper.BiomeHelper.addStructureToBiomeCategories;

@Module(mod = Strange.MOD_ID, description = "Ruined structures.")
public class Ruins extends CharmModule {
    public static final Identifier SURFACE_RUIN_ID = new Identifier(Strange.MOD_ID, "surface_ruin");
    public static final Identifier CAVE_RUIN_ID = new Identifier(Strange.MOD_ID, "cave_ruin");
    public static final Identifier DEEP_RUIN_ID = new Identifier(Strange.MOD_ID, "deep_ruin");
    public static final Identifier NETHER_RUIN_ID = new Identifier(Strange.MOD_ID, "nether_ruin");
    public static final Identifier END_RUIN_ID = new Identifier(Strange.MOD_ID, "end_ruin");

    public static StructureFeature<StructurePoolFeatureConfig> SURFACE_RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> CAVE_RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> DEEP_RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> NETHER_RUIN_FEATURE;
    public static StructureFeature<StructurePoolFeatureConfig> END_RUIN_FEATURE;

    public static ConfiguredStructureFeature<?, ?> SURFACE_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<?, ?> CAVE_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<?, ?> DEEP_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<?, ?> NETHER_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<?, ?> END_RUIN_CONFIGURED;

    @Config(name = "Surface ruin size", description = "Size of the surface ruins.")
    public static int configSurfaceRuinSize = 4;

    @Config(name = "Cave ruin size", description = "Size of the cave ruins.")
    public static int configCaveRuinSize = 7;

    @Config(name = "Deep ruin size", description = "Size of the deep ruins.")
    public static int configDeepRuinSize = 5;

    @Config(name = "Nether ruin size", description = "Size of the nether ruins.")
    public static int configNetherRuinSize = 6;

    @Config(name = "End ruin size", description = "Size of the End ruins.")
    public static int configEndRuinSize = 5;

    @Override
    public void register() {
        SURFACE_RUIN_FEATURE = new SurfaceRuinFeature(StructurePoolFeatureConfig.CODEC);
        CAVE_RUIN_FEATURE = new CaveRuinFeature(StructurePoolFeatureConfig.CODEC);
        DEEP_RUIN_FEATURE = new DeepRuinFeature(StructurePoolFeatureConfig.CODEC);
        NETHER_RUIN_FEATURE = new NetherRuinFeature(StructurePoolFeatureConfig.CODEC);
        END_RUIN_FEATURE = new EndRuinFeature(StructurePoolFeatureConfig.CODEC);

        FabricStructureBuilder.create(SURFACE_RUIN_ID, SURFACE_RUIN_FEATURE)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(18, 8, 1634572)
            .register();

        FabricStructureBuilder.create(CAVE_RUIN_ID, CAVE_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(18, 12, 72319234)
            .register();

        FabricStructureBuilder.create(DEEP_RUIN_ID, DEEP_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(28, 6, 5587267)
            .register();

        FabricStructureBuilder.create(NETHER_RUIN_ID, NETHER_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(18, 8, 78156511)
            .register();

        FabricStructureBuilder.create(END_RUIN_ID, END_RUIN_FEATURE)
            .step(GenerationStep.Feature.UNDERGROUND_STRUCTURES)
            .defaultConfig(24, 6, 78156511)
            .register();

        int surfaceRuinSize = Math.max(0, Math.min(7, configSurfaceRuinSize));
        int caveRuinSize = Math.max(0, Math.min(7, configCaveRuinSize));
        int deepRuinSize = Math.max(0, Math.min(7, configDeepRuinSize));
        int netherRuinSize = Math.max(0, Math.min(7, configNetherRuinSize));
        int endRuinSize = Math.max(0, Math.min(7, configEndRuinSize));

        SURFACE_RUIN_CONFIGURED = SURFACE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> SurfaceRuinGenerator.POOL, surfaceRuinSize));
        CAVE_RUIN_CONFIGURED = CAVE_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> CaveRuinGenerator.POOL, caveRuinSize));
        DEEP_RUIN_CONFIGURED = DEEP_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> DeepRuinGenerator.POOL, deepRuinSize));
        NETHER_RUIN_CONFIGURED = NETHER_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> NetherRuinGenerator.POOL, netherRuinSize));
        END_RUIN_CONFIGURED = END_RUIN_FEATURE.configure(new StructurePoolFeatureConfig(() -> EndRuinGenerator.POOL, endRuinSize));

        // register each configuredFeature with MC registry against the RUIN_STRUCTURE
        configuredStructureFeature(new Identifier(Strange.MOD_ID, "surface_ruin"), SURFACE_RUIN_CONFIGURED);
        configuredStructureFeature(new Identifier(Strange.MOD_ID, "cave_ruin"), CAVE_RUIN_CONFIGURED);
        configuredStructureFeature(new Identifier(Strange.MOD_ID, "deep_ruin"), DEEP_RUIN_CONFIGURED);
        configuredStructureFeature(new Identifier(Strange.MOD_ID, "nether_ruin"), NETHER_RUIN_CONFIGURED);
        configuredStructureFeature(new Identifier(Strange.MOD_ID, "end_ruin"), END_RUIN_CONFIGURED);
    }

    @Override
    public void init() {
        // register ruin loot tables
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_COMMON);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_UNCOMMON);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_RARE);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_EPIC);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.NETHER_RUINS);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.END_RUINS);

        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_RARE);
        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_EPIC);

        addRuins();

        // builds and registers all custom ruins into pools
        SurfaceRuinGenerator.init();
        CaveRuinGenerator.init();
        DeepRuinGenerator.init();
        NetherRuinGenerator.init();
        EndRuinGenerator.init();

        // add registered ruin pools to biomes
        if (!SurfaceRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(SURFACE_RUIN_CONFIGURED, Category.PLAINS);
        if (!CaveRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(CAVE_RUIN_CONFIGURED, Category.PLAINS);
        if (!DeepRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(DEEP_RUIN_CONFIGURED, Category.PLAINS);
        if (!NetherRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(NETHER_RUIN_CONFIGURED, Category.NETHER);
        if (!EndRuinGenerator.RUINS.isEmpty()) addStructureToBiome(END_RUIN_CONFIGURED, BiomeKeys.END_HIGHLANDS);

        // add player location callback
        PlayerState.listeners.add((player, tag) -> {
            if (player != null && player.world != null && !player.world.isClient) {
                ServerWorld serverWorld = (ServerWorld) player.world;
                BlockPos playerPos = player.getBlockPos();
                boolean isInRuin = PosHelper.isInsideStructure(serverWorld, playerPos, SURFACE_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, CAVE_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, DEEP_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, NETHER_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, END_RUIN_FEATURE);

                tag.putBoolean("ruin", isInRuin);
            }
        });
    }

    public static void addRuins() {
        // --- SURFACE ---
        StoneFort stoneFort = new StoneFort();
        SurfaceRuinGenerator.RUINS.add(stoneFort);

        // --- CAVE ---
        Castle castle = new Castle();
        CaveRuinGenerator.RUINS.add(castle);
        CaveRuinGenerator.RUINS.add(castle);
        CaveRuinGenerator.RUINS.add(castle);

        Roguelike roguelike = new Roguelike();
        CaveRuinGenerator.RUINS.add(roguelike);
        CaveRuinGenerator.RUINS.add(roguelike);
        CaveRuinGenerator.RUINS.add(roguelike);

        Vaults vaults = new Vaults();
        CaveRuinGenerator.RUINS.add(vaults);
        CaveRuinGenerator.RUINS.add(vaults);
        CaveRuinGenerator.RUINS.add(vaults);

        // --- DEEP ---
        StoneRoom stoneRoom = new StoneRoom();
        DeepRuinGenerator.RUINS.add(stoneRoom);
        DeepRuinGenerator.RUINS.add(stoneRoom);
    }
}
