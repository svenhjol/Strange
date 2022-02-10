package svenhjol.strange.module.vaults;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.structure.v1.FabricStructureBuilder;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import svenhjol.charm.annotation.CommonModule;
import svenhjol.charm.helper.BiomeHelper;
import svenhjol.charm.helper.WorldHelper;
import svenhjol.charm.loader.CharmModule;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;
import svenhjol.strange.api.event.AddRunestoneDestinationCallback;
import svenhjol.strange.init.StrangeEvents;
import svenhjol.strange.module.runic_tomes.RunicTomes;
import svenhjol.strange.module.vaults.loot.VaultLibraryLootFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Loot ref
 *
 * - strange:vaults/vaults_large_room
 * - strange:vaults/vaults_room
 * - strange:vaults/vaults_corridor
 * - strange:vaults/vaults_forge
 * - strange:vaults/vaults_library
 *
 * //replace stone,cobblestone,mossy_cobblestone,cracked_stone_bricks,mossy_stone_bricks,andesite,gravel stone_bricks
 */
@CommonModule(mod = Strange.MOD_ID, description = "Vaults are large underground structures containing rare loot.\n" +
    "Relics can be found in chests within the large vault rooms.")
public class Vaults extends CharmModule {
    public static final ResourceLocation STRUCTURE_ID = new ResourceLocation(Strange.MOD_ID, "vaults");
    public static final ResourceLocation STARTS = new ResourceLocation(Strange.MOD_ID, "vaults/starts");

    public static LootItemFunctionType LIBRARY_LOOT;
    public static SoundEvent VAULTS_MUSIC;

    public static StructureFeature<JigsawConfiguration> VAULTS_FEATURE;
    public static ConfiguredStructureFeature<JigsawConfiguration, ? extends StructureFeature<JigsawConfiguration>> CONFIGURED_FEATURE;

    public static int vaultsSize = 7;

    public static List<String> dimensionWhitelist = new ArrayList<>(Arrays.asList(
        "minecraft:overworld"
    ));

    public static List<String> biomeCategories = new ArrayList<>(Arrays.asList(
        "mountain"
    ));

    @Override
    public void register() {
        int size = Math.max(0, Math.min(10, vaultsSize));

        VAULTS_FEATURE = new VaultsFeature(JigsawConfiguration.CODEC, STARTS, size, 0, 10);
        CONFIGURED_FEATURE = VAULTS_FEATURE.configured(new JigsawConfiguration(() -> PlainVillagePools.START, 0));

        FabricStructureBuilder.create(STRUCTURE_ID, VAULTS_FEATURE)
            .step(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
            .defaultConfig(40, 36,334020111)
            .register();

        CommonRegistry.configuredStructureFeature(STRUCTURE_ID, CONFIGURED_FEATURE);

        VAULTS_MUSIC = CommonRegistry.sound(new ResourceLocation(Strange.MOD_ID, "vaults_music"));
        LIBRARY_LOOT = CommonRegistry.lootFunctionType(new ResourceLocation(Strange.MOD_ID, "vault_library_loot"), new LootItemFunctionType(new VaultLibraryLootFunction.Serializer()));
    }

    @Override
    public void runWhenEnabled() {
        ServerWorldEvents.LOAD.register(StrangeEvents.WORLD_LOAD_PHASE, this::handleWorldLoad);
        LootTableLoadingCallback.EVENT.register(this::handleLootTables);
        AddRunestoneDestinationCallback.EVENT.register(this::handleAddRunestoneDestination);

        for (String configCategory : biomeCategories) {
            Biome.BiomeCategory category = BiomeHelper.getBiomeCategoryByName(configCategory);
            if (category != null) {
                BiomeHelper.addStructureToBiomeCategory(CONFIGURED_FEATURE, category);
            }
        }

        RunicTomes.interestingDestinations.put(STRUCTURE_ID.toString(), 1.0F);
    }

    private void handleLootTables(ResourceManager resourceManager, LootTables lootTables, ResourceLocation id, FabricLootSupplierBuilder supplier, LootTableLoadingCallback.LootTableSetter lootTableSetter) {
        if (VaultsLoot.VAULTS_LIBRARY.equals(id)) {
            var builder = FabricLootPoolBuilder.builder()
                .rolls(UniformGenerator.between(1, 4))
                .with(LootItem.lootTableItem(Items.BOOK)
                    .setWeight(1)
                    .apply(() -> new VaultLibraryLootFunction(new LootItemCondition[0])));

            supplier.withPool(builder);
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
