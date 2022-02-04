package svenhjol.strange.module.stone_circles;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.AddRunestoneDestinationCallback;
import svenhjol.strange.init.StrangeEvents;
import svenhjol.strange.module.runestones.Runestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "Circles of stone columns with runestones at the top.\n" +
    "Nearly all stone circles contain a runestone that leads you back to spawn point.")
public class StoneCircles extends CharmModule {
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static final ResourceLocation PIECE_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle_piece");
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static LootItemFunctionType LOOT_FUNCTION;

    public static StructurePieceType STONE_CIRCLE_PIECE;
    public static StructureFeature<StoneCircleConfiguration> STONE_CIRCLE_FEATURE;

    public static ConfiguredStructureFeature<?, ?> STONE_CIRCLE_OVERWORLD;
    public static ConfiguredStructureFeature<?, ?> STONE_CIRCLE_NETHER;
    public static ConfiguredStructureFeature<?, ?> STONE_CIRCLE_END;

    @Config(name = "Dimension whitelist", description = "A list of dimensions that stone circles should generate in.")
    public static List<String> dimensionWhitelist = new ArrayList<>(Arrays.asList(
        "minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"
    ));

    @Config(name = "Biome category generation", description = "Biome categories that stone circles may generate in.")
    public static List<String> biomeCategories = List.of(
        "plains", "savanna", "icy", "desert", "swamp", "mountain", "nether", "the_end"
    );

    @Config(name = "Distance between stone circles", description = "Distance between stone circles. As a guide, ruined portals are 25.")
    public static int spacing = 24;

    @Override
    public void register() {
        // register the stone circle structure piece with Charm
        STONE_CIRCLE_PIECE = CommonRegistry.structurePiece(PIECE_ID, StoneCirclePiece::new);

        // init raw structure feature
        STONE_CIRCLE_FEATURE = new StoneCircleFeature(StoneCircleConfiguration.CODEC);

        // register the structure feature with Fabric API
        FabricStructureBuilder.create(STRUCTURE_ID, STONE_CIRCLE_FEATURE)
            .step(GenerationStep.Decoration.SURFACE_STRUCTURES)
            .defaultConfig(spacing, spacing / 3, 515122)
            .register();

        // register each structure feature type with Charm
        STONE_CIRCLE_OVERWORLD = CommonRegistry.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "stone_circle_overworld"), STONE_CIRCLE_FEATURE.configured(new StoneCircleConfiguration(StoneCircleFeature.Type.OVERWORLD)));
        STONE_CIRCLE_NETHER = CommonRegistry.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "stone_circle_nether"), STONE_CIRCLE_FEATURE.configured(new StoneCircleConfiguration(StoneCircleFeature.Type.NETHER)));
        STONE_CIRCLE_END = CommonRegistry.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "stone_circle_end"), STONE_CIRCLE_FEATURE.configured(new StoneCircleConfiguration(StoneCircleFeature.Type.END)));

        // disable further processing if runestones module is not enabled
        this.addDependencyCheck(m -> Strange.LOADER.isEnabled(Runestones.class));
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(StrangeEvents.WORLD_LOAD_PHASE, this::handleWorldLoad);
        AddRunestoneDestinationCallback.EVENT.register(this::handleAddRunestoneDestination);

        // Add configured structures to biome categories.
        // The Nether and The End are special because we want to generate circles with a different style there.
        for (String configCategory : biomeCategories) {
            Biome.BiomeCategory category = Biome.BiomeCategory.byName(configCategory);
            if (category == null) continue;

            switch (category) {
                case NETHER -> BiomeHelper.addStructureToBiomeCategory(STONE_CIRCLE_NETHER, category);
                case THEEND -> BiomeHelper.addStructureToBiomeCategory(STONE_CIRCLE_END, category);
                default -> BiomeHelper.addStructureToBiomeCategory(STONE_CIRCLE_OVERWORLD, category);
            }
        }
    }

    /**
     * Remove the stone circle from structure generation for blacklisted dimensions.
     */
    private void handleWorldLoad(MinecraftServer server, ServerLevel level) {
        if (!dimensionWhitelist.contains(level.dimension().location().toString())) {
            WorldHelper.removeStructures(level, List.of(STONE_CIRCLE_FEATURE));
        }
    }

    /**
     * Add the stone circle as a runestone destination to valid dimensions.
     */
    private void handleAddRunestoneDestination(Level level, LinkedList<ResourceLocation> destinations) {
        if (dimensionWhitelist.contains(level.dimension().location().toString())) {
            if (!destinations.contains(STRUCTURE_ID)) {
                destinations.add(0, STRUCTURE_ID);
            }
        }
    }
}
