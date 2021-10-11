package svenhjol.strange.module.stone_circles;

import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.module.runestones.Runestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID)
public class StoneCircles extends CharmModule {
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static final ResourceLocation PIECE_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle_piece");
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static LootItemFunctionType LOOT_FUNCTION;

    public static StructurePieceType STONE_CIRCLE_PIECE;
    public static StructureFeature<StoneCircleConfiguration> STONE_CIRCLE_STRUCTURE;

    public static ConfiguredStructureFeature<?, ?> STONE_CIRCLE_OVERWORLD;
    public static ConfiguredStructureFeature<?, ?> STONE_CIRCLE_NETHER;
    public static ConfiguredStructureFeature<?, ?> STONE_CIRCLE_END;

    public static List<Biome.BiomeCategory> validBiomeCategories = new ArrayList<>();

    @Config(name = "Biome category generation", description = "Biome categories that stone circles may generate in.")
    public static List<String> configBiomeCategories = new ArrayList<>(Arrays.asList(
        "plains", "savanna", "icy", "desert", "swamp", "mountain", "nether", "the_end"
    ));

    @Config(name = "Distance between stone circles", description = "Distance between stone circles. As a guide, ruined portals are 25.")
    public static int spacing = 24;

    @Override
    public void register() {
        // register the stone circle structure piece with Charm
        STONE_CIRCLE_PIECE = RegistryHelper.structurePiece(PIECE_ID, StoneCirclePiece::new);

        // init raw structure feature
        STONE_CIRCLE_STRUCTURE = new StoneCircleFeature(StoneCircleConfiguration.CODEC);

        // register the structure feature with Fabric API
        FabricStructureBuilder.create(STRUCTURE_ID, STONE_CIRCLE_STRUCTURE)
            .step(GenerationStep.Decoration.SURFACE_STRUCTURES)
            .defaultConfig(spacing, 8, 515122)
            .register();

        // register each structure feature type with Charm
        STONE_CIRCLE_OVERWORLD = RegistryHelper.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "stone_circle_overworld"), STONE_CIRCLE_STRUCTURE.configured(new StoneCircleConfiguration(StoneCircleFeature.Type.OVERWORLD)));
        STONE_CIRCLE_NETHER = RegistryHelper.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "stone_circle_nether"), STONE_CIRCLE_STRUCTURE.configured(new StoneCircleConfiguration(StoneCircleFeature.Type.NETHER)));
        STONE_CIRCLE_END = RegistryHelper.configuredStructureFeature(new ResourceLocation(Strange.MOD_ID, "stone_circle_end"), STONE_CIRCLE_STRUCTURE.configured(new StoneCircleConfiguration(StoneCircleFeature.Type.END)));

        // disable further processing if runestones module is not enabled
        this.addDependencyCheck(m -> Strange.LOADER.isEnabled(Runestones.class));
    }

    @Override
    public void runWhenEnabled() {
        // add configured structures to biome categories. Nether and End are special cases.
        configBiomeCategories.forEach(configCategory -> {
            Biome.BiomeCategory category = Biome.BiomeCategory.byName(configCategory);
            if (category == null) return;

            if (!validBiomeCategories.contains(category)) {
                validBiomeCategories.add(category);
            }

            switch (category) {
                case NETHER -> BiomeHelper.addStructureToBiomeCategories(STONE_CIRCLE_NETHER, category);
                case THEEND -> BiomeHelper.addStructureToBiomeCategories(STONE_CIRCLE_END, category);
                default -> BiomeHelper.addStructureToBiomeCategories(STONE_CIRCLE_OVERWORLD, category);
            }
        });
    }
}
