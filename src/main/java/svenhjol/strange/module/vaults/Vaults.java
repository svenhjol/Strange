package svenhjol.strange.module.vaults;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.dimensions.mirror.MirrorDimension;

import java.util.Arrays;
import java.util.List;

/**
 * Loot ref
 *
 * - strange:vaults/vaults_large_room
 * - strange:vaults/vaults_room
 * - strange:vaults/vaults_corridor
 * - strange:vaults/vaults_forge
 *
 * //replace stone,cobblestone,mossy_cobblestone,cracked_stone_bricks,mossy_stone_bricks,andesite,gravel stone_bricks
 */
@CommonModule(mod = Strange.MOD_ID)
public class Vaults extends CharmModule {
    public static final ResourceLocation VAULTS_ID = new ResourceLocation(Strange.MOD_ID, "vaults");
    public static final ResourceLocation WORLD_LOAD_PHASE = new ResourceLocation(Strange.MOD_ID, "world_load_phase");

    public static StructureFeature<JigsawConfiguration> VAULTS_FEATURE;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CONFIGURED_FEATURE;
    public static StructureProcessorList VAULTS;

    public static int vaultsSize = 7;

    public static boolean onlyInMirrorDimension = true;

    public static List<String> biomeCategories = Arrays.asList(
        "plains", "desert", "mountains", "savanna", "forest", "icy", "mesa"
    );

    @Override
    public void register() {
        int size = Math.max(0, Math.min(10, vaultsSize));
        ResourceLocation starts = new ResourceLocation(Strange.MOD_ID, "vaults/starts");

        VAULTS_FEATURE = new VaultsFeature(JigsawConfiguration.CODEC, starts, size, 8, 16);
        CONFIGURED_FEATURE = VAULTS_FEATURE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 0));

        FabricStructureBuilder.create(VAULTS_ID, VAULTS_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .defaultConfig(32, 18,334020111)
            .register();

        CommonRegistry.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "vaults"), CONFIGURED_FEATURE);
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.addPhaseOrdering(Event.DEFAULT_PHASE, WORLD_LOAD_PHASE);
        ServerWorldEvents.LOAD.register(WORLD_LOAD_PHASE, this::handleWorldLoad);

        for (String configCategory : biomeCategories) {
            Biome.BiomeCategory category = BiomeHelper.getBiomeCategoryByName(configCategory);
            if (category != null) {
                BiomeHelper.addStructureToBiomeCategory(CONFIGURED_FEATURE, category);
            }
        }
    }

    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        if (Strange.LOADER.isEnabled(Dimensions.class)
            && Dimensions.loadMirrorDimension
            && onlyInMirrorDimension
            && !DimensionHelper.isDimension(level, MirrorDimension.ID)
        ) {
            WorldHelper.removeStructures(level, List.of(VAULTS_FEATURE));
        }
    }
}
