package svenhjol.strange.module.stone_circles;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.LogHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeLoot;
import svenhjol.strange.module.runestones.Runestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommonModule(mod = Strange.MOD_ID, description = "Circles of stone columns. Runestones may appear at the top of a column.")
public class StoneCircles extends CharmModule {
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static final ResourceLocation PIECE_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle_piece");
    public static final ResourceLocation LOOT_ID = new ResourceLocation(Strange.MOD_ID, "stone_circle");
    public static LootItemFunctionType LOOT_FUNCTION;

    public static StructurePieceType STONE_CIRCLE_PIECE;
    public static StructureFeature<NoneFeatureConfiguration> STONE_CIRCLE_STRUCTURE;
    public static ConfiguredStructureFeature<?, ?> STONE_CIRCLE;

    @Config(name = "Available biomes", description = "Biomes that stone circles may generate in.")
    public static List<String> configBiomes = new ArrayList<>(Arrays.asList(
        "minecraft:plains",
        "minecraft:desert",
        "minecraft:savanna",
        "minecraft:swamp",
        "minecraft:sunflower_plains",
        "minecraft:flower_forest",
        "minecraft:snowy_tundra",
        "terrestria:cypress_swamp",
        "terrestria:lush_desert",
        "terrestria:volcanic_island",
        "terrestria:dunes",
        "traverse:meadow",
        "traverse:lush_swamp"
    ));

    @Config(name = "Distance between stone circles", description = "Distance between stone circles. As a guide, ruined portals are 25.")
    public static int spacing = 24;

    @Override
    public void register() {
        LOOT_FUNCTION = RegistryHelper.lootFunctionType(LOOT_ID, new LootItemFunctionType(new StoneCircleLootFunction.Serializer()));
        STONE_CIRCLE_STRUCTURE = new StoneCircleFeature(NoneFeatureConfiguration.CODEC);
        STONE_CIRCLE_PIECE = RegistryHelper.structurePiece(PIECE_ID, StoneCircleStructurePiece::new);
        STONE_CIRCLE = RegistryHelper.configuredStructureFeature(STRUCTURE_ID, STONE_CIRCLE_STRUCTURE.configured(NoneFeatureConfiguration.NONE));
        ConfiguredStructureFeature<NoneFeatureConfiguration, ? extends StructureFeature<NoneFeatureConfiguration>> EMPTY = STONE_CIRCLE_STRUCTURE.configured(NoneFeatureConfiguration.NONE);

        FabricStructureBuilder.create(STRUCTURE_ID, STONE_CIRCLE_STRUCTURE)
            .step(GenerationStep.Decoration.SURFACE_STRUCTURES)
            .superflatFeature(EMPTY)
            .defaultConfig(spacing, 8, 515122)
            .register();

        this.addDependencyCheck(m -> Strange.LOADER.isEnabled(Runestones.class));
    }

    @Override
    public void runWhenEnabled() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        configBiomes.forEach(biomeId -> BuiltinRegistries.BIOME.getOptional(new ResourceLocation(biomeId))
            .flatMap(BuiltinRegistries.BIOME::getResourceKey) // flatmap is shorthand for ifPresent(thing) -> return do(thing)
            .ifPresent(biomeKey -> {
                LogHelper.debug(this.getClass(), "Added stone circle to biome: " + biomeId);
                BiomeHelper.addStructureToBiome(STONE_CIRCLE, biomeKey);
            }));
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootManager, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (StrangeLoot.STONE_CIRCLE.equals(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantValue.exactly(1))
                .with(LootItem.lootTableItem(Items.AIR)
                    .setWeight(1)
                    .apply(() -> new StoneCircleLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
        }
    }
}
