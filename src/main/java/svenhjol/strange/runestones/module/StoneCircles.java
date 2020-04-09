package svenhjol.strange.runestones.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.BiomeHelper;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.LocationHelper;
import svenhjol.strange.base.helper.VersionHelper;
import svenhjol.strange.runestones.structure.StoneCirclePiece;
import svenhjol.strange.runestones.structure.StoneCircleStructure;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUNESTONES, hasSubscriptions = true,
    description = "Stone circles are surface structures of stone pillars with a runestone on top.\n" +
        "This module depends on the Runestones module.")
public class StoneCircles extends MesonModule {
    public static final String NAME = "stone_circle";
    public static final String RESNAME = "strange:stone_circle";
    public static Structure<NoFeatureConfig> structure;

    @Config(name = "Add stone circle maps to loot", description = "If true, stone circle maps will be added to village chests.")
    public static boolean addMapsToLoot = true;

    @Config(name = "Allow compasses to detect stone circles", description = "Holding a compass and an iron ingot under a full moon makes the compass point toward the closest stone circle.\n" +
        "Charm's 'Compass Binding' feature must be enabled for this to work.")
    public static boolean compassDetection = true;

    @Config(name = "Distance", description = "Distance between stone cicles. For reference, shipwrecks are 16.")
    public static int distance = 20;

    @Config(name = "Allowed biomes", description = "Biomes that stone circles may generate in.")
    public static List<String> validBiomesConfig = new ArrayList<>(Arrays.asList(
        BiomeHelper.getBiomeName(Biomes.PLAINS),
        BiomeHelper.getBiomeName(Biomes.SUNFLOWER_PLAINS),
        BiomeHelper.getBiomeName(Biomes.BADLANDS),
        BiomeHelper.getBiomeName(Biomes.BADLANDS_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.WOODED_BADLANDS_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.DESERT),
        BiomeHelper.getBiomeName(Biomes.DESERT_LAKES),
        BiomeHelper.getBiomeName(Biomes.BEACH),
        BiomeHelper.getBiomeName(Biomes.RIVER),
        BiomeHelper.getBiomeName(Biomes.SAVANNA),
        BiomeHelper.getBiomeName(Biomes.SAVANNA_PLATEAU),
        BiomeHelper.getBiomeName(Biomes.SNOWY_TUNDRA),
        BiomeHelper.getBiomeName(Biomes.SNOWY_BEACH),
        BiomeHelper.getBiomeName(Biomes.FROZEN_RIVER),
        BiomeHelper.getBiomeName(Biomes.SWAMP)
    ));

    public static final List<Biome> validBiomes = new ArrayList<>();

    @Override
    public boolean shouldRunSetup() {
        return Meson.isModuleEnabled("strange:runestones");
    }

    @Override
    public void init() {
        structure = new StoneCircleStructure();

        RegistryHandler.registerStructure(structure, new ResourceLocation(Strange.MOD_ID, NAME));
        RegistryHandler.registerStructurePiece(StoneCirclePiece.PIECE, new ResourceLocation(Strange.MOD_ID, "scp"));
    }

    @Override
    public void onCommonSetup(FMLCommonSetupEvent event) {
        validBiomesConfig.forEach(biomeName -> {
            //noinspection deprecation
            Biome biome = Registry.BIOME.getOrDefault(new ResourceLocation(biomeName));
            if (!validBiomes.contains(biome)) validBiomes.add(biome);
        });

//        final List<Biome> overworldBiomes = StructureHelper.getOverworldBiomes();

        ForgeRegistries.BIOMES.forEach(biome -> {
        	// This is commented out so modded biomes gets the structure added as a 
        	// feature and won't cut off stone circles if those biomes border a valid biome 
        	// and the stone circle tries to spawn on the border.
//            if (!overworldBiomes.contains(biome))
//                return;

            //Structure can finish generating in any biome so it doesn't get cut off.
            VersionHelper.addStructureToBiomeFeature(structure, biome);
            
            //Only these biomes can start the structure generation.
            if(validBiomes.contains(biome) && Meson.isModuleEnabled("strange:stone_circles"))
                VersionHelper.addStructureToBiomeStructure(structure, biome);
        });
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (!addMapsToLoot) return;

        int weight = 0;
        int quality = 1;

        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_CARTOGRAPHER)) {
            weight = 10;
        } else if (res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_DESERT_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_PLAINS_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SAVANNA_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_SNOWY_HOUSE)
            || res.equals(LootTables.CHESTS_VILLAGE_VILLAGE_TAIGA_HOUSE)
        ) {
            weight = 2;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Items.MAP)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (stack, context) -> {
                    BlockPos pos = context.get(LootParameters.POSITION);
                    if (pos != null)
                        stack = LocationHelper.createMap(context.getWorld(), pos, new ResourceLocation(Strange.MOD_ID, StoneCircles.NAME));

                    return stack;
                })
                .build();

            LootTable table = event.getTable();
            LootHelper.addTableEntry(table, entry);
        }
    }
}
