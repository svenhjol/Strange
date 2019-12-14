package svenhjol.strange.ruins.module;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.registries.ForgeRegistries;
import svenhjol.meson.Meson;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.ruins.feature.StructureBlockProcessor;
import svenhjol.strange.ruins.feature.UndergroundJigsawPiece;
import svenhjol.strange.ruins.structure.UndergroundRuinConfig;
import svenhjol.strange.ruins.structure.UndergroundRuinStructure;

import java.util.*;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.RUINS)
public class UndergroundRuins extends MesonModule
{
    public static Structure<UndergroundRuinConfig> structure;
    public static Map<Biome.Category, List<String>> biomeRuins = new HashMap<>();
    public static Map<String, Map<String, List<String>>> jigsawPieces = new HashMap<>();
    public static List<ResourceLocation> registeredPatterns = new ArrayList<>();
    public static List<Structure<?>> blacklist = new ArrayList<>(Arrays.asList(
        Feature.STRONGHOLD,
        Feature.OCEAN_MONUMENT,
        Feature.NETHER_BRIDGE,
        Feature.BURIED_TREASURE
    ));
    public static final List<String> pieceTypes = Arrays.asList("corridors", "rooms", "starts", "ends", "mobs", "monsters");
    public static final List<String> hasTerminators = Arrays.asList("corridors", "rooms");
    public static final String DIR = "underground";

    @Override
    public void init()
    {
        structure = new UndergroundRuinStructure(UndergroundRuinConfig::deserialize);

        RegistryHandler.registerFeature(structure, new ResourceLocation("underground_ruin"));
        RegistryHandler.registerStructurePiece(UndergroundRuinStructure.UNDERGROUND_RUIN_PIECE, new ResourceLocation(Strange.MOD_ID, "usp"));

        for (Biome biome : ForgeRegistries.BIOMES) {
            biome.addFeature(
                GenerationStage.Decoration.UNDERGROUND_STRUCTURES,
                Biome.createDecoratedFeature(structure, new UndergroundRuinConfig(1.0F), Placement.NOPE, IPlacementConfig.NO_PLACEMENT_CONFIG));

            biome.addStructure(structure, new UndergroundRuinConfig(1.0F));
        }
    }

    @Override
    public void serverStarted(FMLServerStartedEvent event)
    {
        IReloadableResourceManager rm = event.getServer().getResourceManager();

        List<StructureProcessor> processors = Arrays.asList(
            new StructureBlockProcessor(),
            new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.LIGHT_BLUE_STAINED_GLASS))
        );

        try {
            biomeRuins = new HashMap<>();
            jigsawPieces = new HashMap<>();

            for (Biome.Category cat : Biome.Category.values()) {
                String catName = cat.getName().toLowerCase();
                Collection<ResourceLocation> resources = rm.getAllResourceLocations("structures/" + DIR + "/" + catName, file -> file.endsWith(".nbt"));
                if (!biomeRuins.containsKey(cat)) biomeRuins.put(cat, new ArrayList<>());

                for (ResourceLocation res : resources) {
                    String name, ruin;

                    String[] p = res.getPath().split("/");
                    if (p.length != 5) continue;

                    ruin = p[3];
                    if (!biomeRuins.get(cat).contains(ruin)) {
                        biomeRuins.get(cat).add(ruin);
                    }

                    name = res.getPath()
                        .replace(".nbt", "")
                        .replace("structures/", "");

                    if (!jigsawPieces.containsKey(ruin)) jigsawPieces.put(ruin, new HashMap<>());

                    for (String pieceType : pieceTypes) {
                        if (!jigsawPieces.get(ruin).containsKey(pieceType)) jigsawPieces.get(ruin).put(pieceType, new ArrayList<>());
                        if (name.contains(pieceType.substring(0, pieceType.length()-1))) jigsawPieces.get(ruin).get(pieceType).add(name);
                    }
                }

                for (String ruin : jigsawPieces.keySet()) {
                    // used to close off pathways
                    ResourceLocation ends = new ResourceLocation(Strange.MOD_ID, DIR + "/" + catName + "/" + ruin + "/ends");

                    for (String pieceType : pieceTypes) {
                        ResourceLocation patternId = new ResourceLocation(Strange.MOD_ID, DIR + "/" + catName + "/" + ruin + "/" + pieceType);
                        if (registeredPatterns.contains(patternId)) continue;

                        List<String> pieces = jigsawPieces.get(ruin).get(pieceType);
                        List<Pair<JigsawPiece, Integer>> piecesAndWeights = new ArrayList<>();

                        for (String piece : pieces) {
                            SingleJigsawPiece jigsawPiece = new UndergroundJigsawPiece(Strange.MOD_ID + ":" + piece, processors);
                            piecesAndWeights.add(Pair.of(jigsawPiece, UndergroundRuinStructure.getWeight("_w", piece, 1)));
                        }

                        ResourceLocation end = hasTerminators.contains(pieceType) ? ends : new ResourceLocation("empty");

                        if (piecesAndWeights.size() > 0) {
                            JigsawPattern pattern = new JigsawPattern(patternId, end, piecesAndWeights, JigsawPattern.PlacementBehaviour.RIGID);
                            JigsawManager.REGISTRY.register(pattern);
                        }
                        registeredPatterns.add(patternId);
                    }
                }

            }
        } catch (Exception e) {
            Meson.warn("Could not load structures for biome category", e);
        }

        Meson.log(biomeRuins);
    }
}
