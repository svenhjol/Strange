package svenhjol.strange.module.vaults;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.AddRunestoneDestinationCallback;
import svenhjol.strange.init.StrangeEvents;

import java.util.LinkedList;
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
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(Strange.MOD_ID, "vaults");
    public static final ResourceLocation STARTS = new ResourceLocation(Strange.MOD_ID, "vaults/starts");

    public static StructureFeature<JigsawConfiguration> VAULTS_FEATURE;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CONFIGURED_FEATURE;

    public static int vaultsSize = 7;

    public static List<String> dimensionWhitelist = List.of(
        "strange:mirror"
    );

    public static List<String> biomeCategories = List.of(
        "plains", "desert", "mountains", "savanna", "forest", "icy", "mesa"
    );

    @Override
    public void register() {
        int size = Math.max(0, Math.min(10, vaultsSize));

        VAULTS_FEATURE = new VaultsFeature(JigsawConfiguration.CODEC, STARTS, size, 8, 16);
        CONFIGURED_FEATURE = VAULTS_FEATURE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 0));

        FabricStructureBuilder.create(STRUCTURE_ID, VAULTS_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .defaultConfig(40, 36,334020111)
            .register();

        CommonRegistry.configuredStructureFeature(STRUCTURE_ID, CONFIGURED_FEATURE);
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(StrangeEvents.WORLD_LOAD_PHASE, this::handleWorldLoad);
        AddRunestoneDestinationCallback.EVENT.register(this::handleAddRunestoneDestination);

        for (String configCategory : biomeCategories) {
            Biome.BiomeCategory category = BiomeHelper.getBiomeCategoryByName(configCategory);
            if (category != null) {
                BiomeHelper.addStructureToBiomeCategory(CONFIGURED_FEATURE, category);
            }
        }
    }

    private void handleAddRunestoneDestination(Level level, LinkedList<ResourceLocation> destinations) {
        if (dimensionWhitelist.contains(level.dimension().location().toString())) {
            if (!destinations.contains(STRUCTURE_ID)) {
                int size = destinations.size();
                destinations.add(size, STRUCTURE_ID);
            }
        }
    }

    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        if (!dimensionWhitelist.contains(level.dimension().location().toString())) {
            WorldHelper.removeStructures(level, List.of(VAULTS_FEATURE));
        }
    }
}
