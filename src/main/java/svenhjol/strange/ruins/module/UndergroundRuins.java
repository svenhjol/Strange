package svenhjol.strange.ruins.module;

import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.helper.LootHelper;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.helper.StructureHelper;
import svenhjol.strange.base.helper.StructureHelper.RegisterJigsawPieces;
import svenhjol.strange.ruins.structure.MarkerPiece;
import svenhjol.strange.ruins.structure.UndergroundPiece;
import svenhjol.strange.ruins.structure.UndergroundStructure;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUINS, hasSubscriptions = true,
    description = "Ruins that spawn underground with different types according to biome.")
public class UndergroundRuins extends MesonModule {
    public static final String NAME = "underground_ruin";
    public static final String RESNAME = "strange:underground_ruin";
    public static final String DIR = "underground";
    public static Structure<NoFeatureConfig> structure;
    public static final Map<Biome.Category, List<String>> ruins = new HashMap<>();
    public static final Map<Biome.Category, Map<String, Integer>> sizes = new HashMap<>();
    public static final Map<Biome.Category, List<ResourceLocation>> starts = new HashMap<>();
    public static final List<Structure<?>> blacklist = new ArrayList<>(Arrays.asList(
        Feature.STRONGHOLD,
        Feature.OCEAN_MONUMENT
    ));

    @Config(name = "Default size", description = "Default ruin size.")
    public static int defaultSize = 2;

    @Config(name = "Distance", description = "Distance between ruin structures. For reference, shipwrecks are 16.")
    public static int distance = 20;

    @Config(name = "Additional pieces", description = "Ruin size is increased randomly by this amount.")
    public static int variation = 2;

    @Config(name = "Add marker", description = "Add a stone marker on the surface above a ruin.")
    public static boolean addMarker = true;

    @Config(name = "Add underground ruin maps to loot", description = "If true, underground ruin maps will be added to dungeon loot and nether fortress chests.")
    public static boolean addMapsToLoot = true;

    @Config(name = "Marker chance", description = "Chance (out of 1.0) of a marker spawning on the surface above a ruin.")
    public static double markerChance = 0.75D;

    @Override
    public void init() {
        structure = new UndergroundStructure();

        RegistryHandler.registerStructure(structure, new ResourceLocation(Strange.MOD_ID, NAME));
        RegistryHandler.registerStructurePiece(UndergroundPiece.PIECE, new ResourceLocation(Strange.MOD_ID, "usp"));
        RegistryHandler.registerStructurePiece(MarkerPiece.PIECE, new ResourceLocation(Strange.MOD_ID, "ump"));


        final List<Biome> endBiomes = StructureHelper.getEndBiomes();

        for (Biome biome : ForgeRegistries.BIOMES) {
            if (endBiomes.contains(biome))
                continue;

            biome.addFeature(
                GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
                Biome.createDecoratedFeature(structure, IFeatureConfig.NO_FEATURE_CONFIG, Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));

            biome.addStructure(structure, NoFeatureConfig.NO_FEATURE_CONFIG);
        }
    }

    @Override
    public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        // don't spawn near Quark's big dungeons
        if (Strange.quarkCompat != null
            && Strange.quarkCompat.hasBigDungeons()
        ) {
            Structure<?> structure = Strange.quarkCompat.getBigDungeonStructure();
            if (!blacklist.contains(structure)) {
                blacklist.add(structure);
                Strange.LOG.debug("[UndergroundRuins] Added Quark's Big Dungeons to underground ruin blacklist");
            }
        }

        // don't spawn near Vaults
        if (Meson.isModuleEnabled("strange:vaults")) {
            if (!blacklist.contains(Vaults.structure)) {
                blacklist.add(Vaults.structure);
                Strange.LOG.debug("[UndergroundRuins] Added Vaults to underground ruin blacklist");
            }
        }
    }

    @Override
    public void onServerStarted(FMLServerStartedEvent event) {
        IReloadableResourceManager rm = event.getServer().getResourceManager();

        for (Biome.Category cat : Biome.Category.values()) {
            String catName = cat.getName().toLowerCase();
            String dir = DIR + "/" + catName;
            RegisterJigsawPieces register = new RegisterJigsawPieces(rm, dir);

            if (register.structures.size() == 0) continue;

            if (!ruins.containsKey(cat)) ruins.put(cat, new ArrayList<>());
            ruins.get(cat).addAll(register.structures);

            if (!starts.containsKey(cat)) starts.put(cat, new ArrayList<>());
            starts.get(cat).addAll(register.starts);

            if (!sizes.containsKey(cat)) sizes.put(cat, new HashMap<>());
            sizes.get(cat).putAll(register.sizes);
        }

        Strange.LOG.debug("Ruins data: " + ruins.toString());
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (!addMapsToLoot) return;

        int weight = 0;
        int quality = 1;

        ResourceLocation res = event.getName();

        if (res.equals(LootTables.CHESTS_SIMPLE_DUNGEON)) {
            weight = 10;
        } else if (res.equals(LootTables.CHESTS_NETHER_BRIDGE)) {
            weight = 16;
        }

        if (weight > 0) {
            LootEntry entry = ItemLootEntry.builder(Items.MAP)
                .weight(weight)
                .quality(quality)
                .acceptFunction(() -> (stack, context) -> {
                    BlockPos pos = context.get(LootParameters.POSITION);
                    if (pos != null) {
                        ServerWorld world = context.getWorld();
                        BlockPos structurePos = world.findNearestStructure(RESNAME, pos, 100, true);
                        if (structurePos != null) {
                            ItemStack map = FilledMapItem.setupNewMap(world, structurePos.getX(), structurePos.getZ(), (byte) 2, true, true);
                            FilledMapItem.func_226642_a_(world, map);
                            MapData.addTargetDecoration(map, structurePos, "+", MapDecoration.Type.TARGET_X);
                            map.setDisplayName(new TranslationTextComponent("filled_map.underground_ruin"));
                            return map;
                        }
                    }
                    return stack;
                })
                .build();

            LootTable table = event.getTable();
            LootHelper.addTableEntry(table, entry);
        }
    }
}
