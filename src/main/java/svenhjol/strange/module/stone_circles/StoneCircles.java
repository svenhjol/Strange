package svenhjol.strange.module.stone_circles;

import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.item.Items;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import svenhjol.charm.Charm;
import svenhjol.charm.annotation.Config;
import svenhjol.charm.annotation.Module;
import svenhjol.charm.handler.ModuleHandler;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.charm.module.CharmModule;
import svenhjol.strange.Strange;
import svenhjol.strange.init.StrangeLoot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Module(mod = Strange.MOD_ID, description = "Circles of stone columns. Runestones may appear at the top of a column.")
public class StoneCircles extends CharmModule {
    public static final Identifier STRUCTURE_ID = new Identifier(Strange.MOD_ID, "stone_circle");
    public static final Identifier PIECE_ID = new Identifier(Strange.MOD_ID, "stone_circle_piece");
    public static final Identifier LOOT_ID = new Identifier(Strange.MOD_ID, "stone_circle");
    public static LootFunctionType LOOT_FUNCTION;

    public static StructurePieceType STONE_CIRCLE_PIECE;
    public static StructureFeature<DefaultFeatureConfig> STONE_CIRCLE_STRUCTURE;
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
        "traverse:meadow",
        "traverse:lush_swamp"
    ));

    @Config(name = "Distance between stone circles", description = "Distance between stone circles. As a guide, ruined portals are 25.")
    public static int spacing = 24;

    @Override
    public void register() {
        LOOT_FUNCTION = RegistryHelper.lootFunctionType(LOOT_ID, new LootFunctionType(new StoneCircleLootFunction.Serializer()));
        STONE_CIRCLE_STRUCTURE = new StoneCircleFeature(DefaultFeatureConfig.CODEC);
        STONE_CIRCLE_PIECE = RegistryHelper.structurePiece(PIECE_ID, StoneCircleStructurePiece::new);

        FabricStructureBuilder.create(STRUCTURE_ID, STONE_CIRCLE_STRUCTURE)
            .step(GenerationStep.Feature.SURFACE_STRUCTURES)
            .defaultConfig(spacing, 8, 515122)
            .register();

        STONE_CIRCLE = RegistryHelper.configuredStructureFeature(STRUCTURE_ID, STONE_CIRCLE_STRUCTURE.configure(DefaultFeatureConfig.DEFAULT));
    }

    @Override
    public boolean depends() {
        return ModuleHandler.enabled("strange:runestones");
    }

    @Override
    public void init() {
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);

        configBiomes.forEach(biomeId -> BuiltinRegistries.BIOME.getOrEmpty(new Identifier(biomeId))
            .flatMap(BuiltinRegistries.BIOME::getKey) // flatmap is shorthand for ifPresent(thing) -> return do(thing)
            .ifPresent(biomeKey -> {
                Charm.LOG.debug("[StoneCircles] Added stone circle to biome: " + biomeId);
                BiomeHelper.addStructureToBiome(STONE_CIRCLE, biomeKey);
            }));
    }

    private void handleLootTables(ResourceManager resourceManager, LootManager lootManager, Identifier id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter setter) {
        if (StrangeLoot.STONE_CIRCLE.equals(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(Items.AIR)
                    .weight(1)
                    .apply(() -> new StoneCircleLootFunction(new LootCondition[0])));

            supplier.pool(builder);
        }
    }
}
