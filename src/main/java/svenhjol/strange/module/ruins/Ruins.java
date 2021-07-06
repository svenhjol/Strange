package svenhjol.strange.module.ruins;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.api.CharmPlayerStateKeys;
import svenhjol.charm.helper.DecorationHelper;
import svenhjol.charm.helper.LootHelper;
import svenhjol.charm.helper.PosHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.module.player_state.PlayerState;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeLoot;
import svenhjol.strange.module.ruins.feature.*;
import svenhjol.strange.module.ruins.generator.*;

import static svenhjol.charm.helper.BiomeHelper.addStructureToBiome;
import static svenhjol.charm.helper.BiomeHelper.addStructureToBiomeCategories;
import static svenhjol.charm.helper.RegistryHelper.configuredStructureFeature;

@CommonModule(mod = Strange.MOD_ID, description = "Ruined structures found on the surface, in caves, and at the deepest levels of the overworld.")
public class Ruins extends CharmModule {
    public static final ResourceLocation SURFACE_RUIN_ID = new ResourceLocation(Strange.MOD_ID, "surface_ruin");
    public static final ResourceLocation CAVE_RUIN_ID = new ResourceLocation(Strange.MOD_ID, "cave_ruin");
    public static final ResourceLocation DEEP_RUIN_ID = new ResourceLocation(Strange.MOD_ID, "deep_ruin");
    public static final ResourceLocation NETHER_RUIN_ID = new ResourceLocation(Strange.MOD_ID, "nether_ruin");
    public static final ResourceLocation END_RUIN_ID = new ResourceLocation(Strange.MOD_ID, "end_ruin");

    public static StructureFeature<JigsawConfiguration> SURFACE_RUIN_FEATURE;
    public static StructureFeature<JigsawConfiguration> CAVE_RUIN_FEATURE;
    public static StructureFeature<JigsawConfiguration> DEEP_RUIN_FEATURE;
    public static StructureFeature<JigsawConfiguration> NETHER_RUIN_FEATURE;
    public static StructureFeature<JigsawConfiguration> END_RUIN_FEATURE;

    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> SURFACE_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CAVE_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> DEEP_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> NETHER_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> END_RUIN_CONFIGURED;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> EMPTY_CONFIGURED;

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
        SURFACE_RUIN_FEATURE = new SurfaceRuinFeature(JigsawConfiguration.CODEC);
        CAVE_RUIN_FEATURE = new CaveRuinFeature(JigsawConfiguration.CODEC);
        DEEP_RUIN_FEATURE = new DeepRuinFeature(JigsawConfiguration.CODEC);
        NETHER_RUIN_FEATURE = new NetherRuinFeature(JigsawConfiguration.CODEC);
        END_RUIN_FEATURE = new EndRuinFeature(JigsawConfiguration.CODEC);

        int surfaceRuinSize = Math.max(0, Math.min(7, configSurfaceRuinSize));
        int caveRuinSize = Math.max(0, Math.min(7, configCaveRuinSize));
        int deepRuinSize = Math.max(0, Math.min(7, configDeepRuinSize));
        int netherRuinSize = Math.max(0, Math.min(7, configNetherRuinSize));
        int endRuinSize = Math.max(0, Math.min(7, configEndRuinSize));

        SURFACE_RUIN_CONFIGURED = SURFACE_RUIN_FEATURE.configured(new JigsawConfiguration(() -> SurfaceRuinGenerator.POOL, surfaceRuinSize));
        CAVE_RUIN_CONFIGURED = CAVE_RUIN_FEATURE.configured(new JigsawConfiguration(() -> CaveRuinGenerator.POOL, caveRuinSize));
        DEEP_RUIN_CONFIGURED = DEEP_RUIN_FEATURE.configured(new JigsawConfiguration(() -> DeepRuinGenerator.POOL, deepRuinSize));
        NETHER_RUIN_CONFIGURED = NETHER_RUIN_FEATURE.configured(new JigsawConfiguration(() -> NetherRuinGenerator.POOL, netherRuinSize));
        END_RUIN_CONFIGURED = END_RUIN_FEATURE.configured(new JigsawConfiguration(() -> EndRuinGenerator.POOL, endRuinSize));
        EMPTY_CONFIGURED = CAVE_RUIN_FEATURE.configured(new JigsawConfiguration(() -> CaveRuinGenerator.EMPTY_POOL, 0));

        FabricStructureBuilder.create(SURFACE_RUIN_ID, SURFACE_RUIN_FEATURE)
            .step(GenerationStep.Decoration.SURFACE_STRUCTURES)
            .superflatFeature(EMPTY_CONFIGURED)
            .defaultConfig(18, 8, 1634572)
            .register();

        FabricStructureBuilder.create(CAVE_RUIN_ID, CAVE_RUIN_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .superflatFeature(EMPTY_CONFIGURED)
            .defaultConfig(18, 12, 72319234)
            .register();

        FabricStructureBuilder.create(DEEP_RUIN_ID, DEEP_RUIN_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .superflatFeature(EMPTY_CONFIGURED)
            .defaultConfig(28, 6, 5587267)
            .register();

        FabricStructureBuilder.create(NETHER_RUIN_ID, NETHER_RUIN_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .superflatFeature(EMPTY_CONFIGURED)
            .defaultConfig(18, 8, 78156511)
            .register();

        FabricStructureBuilder.create(END_RUIN_ID, END_RUIN_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .superflatFeature(EMPTY_CONFIGURED)
            .defaultConfig(32, 6, 78156511)
            .register();

        // register each configuredFeature with MC registry
        configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "surface_ruin"), SURFACE_RUIN_CONFIGURED);
        configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "cave_ruin"), CAVE_RUIN_CONFIGURED);
        configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "deep_ruin"), DEEP_RUIN_CONFIGURED);
        configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "nether_ruin"), NETHER_RUIN_CONFIGURED);
        configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "end_ruin"), END_RUIN_CONFIGURED);

        RuinBuilds.init();

        // builds and registers all custom ruins into pools
        SurfaceRuinGenerator.init();
        CaveRuinGenerator.init();
        DeepRuinGenerator.init();
        NetherRuinGenerator.init();
        EndRuinGenerator.init();
    }

    @Override
    public void runWhenEnabled() {
        // register ruin loot tables
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_COMMON);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_UNCOMMON);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_RARE);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_EPIC);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.NETHER_RUINS);
        LootHelper.CUSTOM_LOOT_TABLES.add(StrangeLoot.END_RUINS);

        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_RARE);
        DecorationHelper.RARE_CHEST_LOOT_TABLES.add(StrangeLoot.OVERWORLD_RUINS_EPIC);

        // add registered ruin pools to biomes
        if (!SurfaceRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(SURFACE_RUIN_CONFIGURED, BiomeCategory.PLAINS);
        if (!CaveRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(CAVE_RUIN_CONFIGURED, BiomeCategory.PLAINS);
        if (!DeepRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(DEEP_RUIN_CONFIGURED, BiomeCategory.PLAINS);
        if (!NetherRuinGenerator.RUINS.isEmpty()) addStructureToBiomeCategories(NETHER_RUIN_CONFIGURED, BiomeCategory.NETHER);
        if (!EndRuinGenerator.RUINS.isEmpty()) addStructureToBiome(END_RUIN_CONFIGURED, Biomes.END_HIGHLANDS);

        PlayerState.addCallback((player, nbt) -> {
            if (player != null && player.level != null && !player.level.isClientSide) {
                ServerLevel serverWorld = (ServerLevel) player.level;
                BlockPos playerPos = player.blockPosition();
                boolean isInRuin = PosHelper.isInsideStructure(serverWorld, playerPos, SURFACE_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, CAVE_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, DEEP_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, NETHER_RUIN_FEATURE)
                    || PosHelper.isInsideStructure(serverWorld, playerPos, END_RUIN_FEATURE);

                // TODO: this is dumb, we need a ruin key
                nbt.putBoolean(CharmPlayerStateKeys.InsideStronghold.toString(), isInRuin);
            }
        });
    }
}
