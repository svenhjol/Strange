package svenhjol.strange.ruins.module;

import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.base.StructureHelper.RegisterJigsawPieces;
import svenhjol.strange.ruins.structure.UndergroundConfig;
import svenhjol.strange.ruins.structure.UndergroundStructure;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUINS)
public class UndergroundRuins extends MesonModule
{
    public static final String DIR = "underground";
    public static Structure<UndergroundConfig> structure;
    public static Map<Biome.Category, List<String>> ruins = new HashMap<>();
    public static Map<Biome.Category, Map<String, Integer>> sizes = new HashMap<>();
    public static Map<Biome.Category, List<ResourceLocation>> starts = new HashMap<>();
    public static List<Structure<?>> blacklist = new ArrayList<>(Arrays.asList(
        Feature.STRONGHOLD,
        Feature.OCEAN_MONUMENT,
        Feature.NETHER_BRIDGE,
        Feature.BURIED_TREASURE
    ));

    @Config(name = "Default size", description = "Controls how many pieces generate as part of a ruin.")
    public static int defaultSize = 2;

    @Config(name = "Additional pieces", description = "Random number of extra pieces that may be added to a ruin.")
    public static int variation = 1;

    @Override
    public void init()
    {
        structure = new UndergroundStructure(UndergroundConfig::deserialize);

        RegistryHandler.registerFeature(structure, new ResourceLocation("underground_ruin"));
        RegistryHandler.registerStructurePiece(UndergroundStructure.UNDERGROUND_PIECE, new ResourceLocation(Strange.MOD_ID, "usp"));

        for (Biome biome : ForgeRegistries.BIOMES) {
            biome.addFeature(
                GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
                Biome.createDecoratedFeature(structure, new UndergroundConfig(1.0F), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));

            biome.addStructure(structure, new UndergroundConfig(1.0F));
        }
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event)
    {
        IReloadableResourceManager rm = event.getServer().getResourceManager();

        for (Biome.Category cat : Biome.Category.values()) {
            String catName = cat.getName().toLowerCase();
            String dir = DIR + "/" + catName;
            RegisterJigsawPieces register = new RegisterJigsawPieces(rm, dir);

            if (register.ruins.size() == 0) continue;

            if (!ruins.containsKey(cat)) ruins.put(cat, new ArrayList<>());
            ruins.get(cat).addAll(register.ruins);

            if (!starts.containsKey(cat)) starts.put(cat, new ArrayList<>());
            starts.get(cat).addAll(register.starts);

            if (!sizes.containsKey(cat)) sizes.put(cat, new HashMap<>());
            sizes.get(cat).putAll(register.sizes);
        }

        Meson.log(ruins);
    }

//    @Override
//    public void serverStarted(FMLServerStartedEvent event)
//    {
//        IReloadableResourceManager rm = event.getServer().getResourceManager();
//
//        List<StructureProcessor> processors = Arrays.asList(
//            new StructureBlockProcessor(),
//            new AirBlockProcessor(),
//            new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.GRAY_STAINED_GLASS))
//        );
//
//        try {
//            ruinBiomes = new HashMap<>();
//            ruinPieces = new HashMap<>();
//            ruinSizes = new HashMap<>();
//
//            for (Biome.Category cat : Biome.Category.values()) {
//                String catName = cat.getName().toLowerCase();
//
//                Collection<ResourceLocation> resources = rm.getAllResourceLocations("structures/" + DIR + "/" + catName, file -> file.endsWith(".nbt"));
//                if (!ruinBiomes.containsKey(cat)) ruinBiomes.put(cat, new ArrayList<>());
//
//                for (ResourceLocation res : resources) {
//                    String path, ruin;
//
//                    String[] p = res.getPath().split("/");
//                    if (p.length != 5) continue;
//                    ruin = p[3];
//
//                    if (!ruinBiomes.get(cat).contains(ruin))
//                        ruinBiomes.get(cat).add(ruin);
//
//                    path = res.getPath().replace(".nbt", "").replace("structures/", "");
//
//                    // create the ruin pieces tree structure for adding ruin piece types to
//                    if (!ruinPieces.containsKey(cat))
//                        ruinPieces.put(cat, new HashMap<>());
//
//                    if (!ruinPieces.get(cat).containsKey(ruin))
//                        ruinPieces.get(cat).put(ruin, new HashMap<>());
//
//                    for (String pieceType : pieceTypes) {
//                        if (!ruinPieces.get(cat).get(ruin).containsKey(pieceType))
//                            ruinPieces.get(cat).get(ruin).put(pieceType, new ArrayList<>());
//
//                        if (path.contains(pieceType.substring(0, pieceType.length() - 1)))
//                            ruinPieces.get(cat).get(ruin).get(pieceType).add(path);
//                    }
//                }
//            }
//
//            for (Biome.Category cat : ruinPieces.keySet()) {
//                String catName = cat.getName().toLowerCase();
//
//                for (String ruin : ruinPieces.get(cat).keySet()) {
//                    int size = 0;
//
//                    for (String pieceType : pieceTypes) {
//                        ResourceLocation patternId = new ResourceLocation(Strange.MOD_ID, DIR + "/" + catName + "/" + ruin + "/" + pieceType);
//                        if (registered.contains(patternId))
//                            continue;
//
//                        List<String> pieces = ruinPieces.get(cat).get(ruin).get(pieceType);
//                        List<Pair<JigsawPiece, Integer>> piecesAndWeights = new ArrayList<>();
//
//                        for (String piece : pieces) {
//                            size = Math.max(size, UndergroundRuinStructure.getWeight("_s", piece, 0));
//                            SingleJigsawPiece jigsawPiece = new UndergroundJigsawPiece(Strange.MOD_ID + ":" + piece, processors);
//                            piecesAndWeights.add(Pair.of(jigsawPiece, UndergroundRuinStructure.getWeight("_w", piece, 1)));
//                        }
//
//                        // used to close off pathways
//                        ResourceLocation end = hasTerminators.contains(pieceType) ? new ResourceLocation(Strange.MOD_ID, DIR + "/" + catName + "/" + ruin + "/ends") : new ResourceLocation("empty");
//
//                        if (piecesAndWeights.size() > 0) {
//                            JigsawPattern pattern = new JigsawPattern(patternId, end, piecesAndWeights, JigsawPattern.PlacementBehaviour.RIGID);
//                            JigsawManager.REGISTRY.register(pattern);
//                        }
//                        registered.add(patternId);
//                    }
//
//                    ruinSizes.put(catName + ":" + ruin, Math.max(2, size));
//                }
//            }
//        } catch (Exception e) {
//            Meson.warn("Could not load structures for biome category", e);
//        }
//
//        Meson.log(ruinBiomes);
//    }
}
