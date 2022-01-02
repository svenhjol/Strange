package svenhjol.strange.module.end_shrines;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.DimensionHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.AddRunestoneDestinationCallback;
import svenhjol.strange.module.dimensions.Dimensions;
import svenhjol.strange.module.dimensions.floating_islands.FloatingIslandsDimension;
import svenhjol.strange.module.dimensions.mirror.MirrorDimension;
import svenhjol.strange.module.end_shrines.processor.EndShrinePortalProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class EndShrines extends CharmModule {
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(Strange.MOD_ID, "end_shrine");
    public static final ResourceLocation BLOCK_ID = new ResourceLocation(Strange.MOD_ID, "end_shrine_portal");
    public static EndShrinePortalBlock END_SHRINE_PORTAL_BLOCK;
    public static BlockEntityType<EndShrinePortalBlockEntity> END_SHRINE_PORTAL_BLOCK_ENTITY;

    public static final ResourceLocation STARTS = new ResourceLocation(Strange.MOD_ID, "end_shrines/starts");
    public static StructureFeature<JigsawConfiguration> END_SHRINE_FEATURE;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CONFIGURED_FEATURE;
    public static StructureProcessorType<EndShrinePortalProcessor> END_SHRINE_PORTAL_PROCESSOR;

    public static List<ResourceLocation> DESTINATIONS = new ArrayList<>();
    public static List<String> BIOMES;

    public static List<String> additionalBiomes = List.of();

    @Override
    public void register() {
        END_SHRINE_PORTAL_BLOCK = new EndShrinePortalBlock(this);
        END_SHRINE_PORTAL_BLOCK_ENTITY = CommonRegistry.blockEntity(BLOCK_ID, EndShrinePortalBlockEntity::new, END_SHRINE_PORTAL_BLOCK);

        END_SHRINE_FEATURE = new EndShrineFeature(JigsawConfiguration.CODEC, STARTS, 1, 150, 25);
        CONFIGURED_FEATURE = END_SHRINE_FEATURE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 0));

        FabricStructureBuilder.create(STRUCTURE_ID, END_SHRINE_FEATURE)
            .step(GenerationStep.Decoration.SURFACE_STRUCTURES)
            .defaultConfig(40, 32,1255213011)
            .register();

        CommonRegistry.configuredStructureFeature(STRUCTURE_ID, CONFIGURED_FEATURE);

        END_SHRINE_PORTAL_PROCESSOR = CommonRegistry.structureProcessor(new ResourceLocation(Strange.MOD_ID, "end_shrine_portal"), () -> EndShrinePortalProcessor.CODEC);
    }

    @Override
    public void runWhenEnabled() {
        AddRunestoneDestinationCallback.EVENT.register(this::handleAddRunestoneDestination);

        DESTINATIONS.add(Level.OVERWORLD.location());

        if (Dimensions.mirrorEnabled()) {
            DESTINATIONS.add(MirrorDimension.ID);
        }

        if (Dimensions.floatingIslandsEnabled()) {
            DESTINATIONS.add(FloatingIslandsDimension.ID);
        }

        // Add the End Shrine structure to the biomes in the config.
        BIOMES.addAll(additionalBiomes);

        for (String allowedBiome : BIOMES) {
            var biome = BuiltinRegistries.BIOME.get(new ResourceLocation(allowedBiome));
            if (biome == null) continue;

            var biomeKey = BiomeHelper.getBiomeKeyFromBiome(biome);
            BiomeHelper.addStructureToBiome(CONFIGURED_FEATURE, biomeKey);
        }
    }

    private void handleAddRunestoneDestination(Level level, LinkedList<ResourceLocation> destinations) {
        if (DimensionHelper.isEnd(level)) {
            destinations.add(STRUCTURE_ID);
        }
    }

    static {
        BIOMES = new ArrayList<>(Arrays.asList(
            "end_highlands", "end_midlands", "small_end_islands", "end_barrens"
        ));
    }
}
