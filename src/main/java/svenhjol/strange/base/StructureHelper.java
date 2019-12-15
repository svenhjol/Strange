package svenhjol.strange.base;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.ruins.feature.AirBlockProcessor;
import svenhjol.strange.ruins.feature.StructureBlockProcessor;
import svenhjol.strange.ruins.feature.UndergroundJigsawPiece;
import svenhjol.strange.ruins.structure.UndergroundStructure;

import java.util.*;

public class StructureHelper
{
    public static List<ResourceLocation> registeredPieces = new ArrayList<>();

    public static class RegisterJigsawPieces
    {
        public static final String STRUCTURES = "structures";
        public static final String CORRIDORS = "corridors";
        public static final String ROOMS = "rooms";
        public static final String STARTS = "starts";
        public static final String ENDS = "ends";

        public List<String> ruins = new ArrayList<>();
        public List<ResourceLocation> starts = new ArrayList<>();
        public Map<String, Integer> sizes = new HashMap<>();

        protected final List<String> pieceTypes = Arrays.asList(CORRIDORS, ROOMS, STARTS, ENDS);
        protected final List<String> hasEnds = Arrays.asList(CORRIDORS, ROOMS);

        public RegisterJigsawPieces(IReloadableResourceManager rm, String structuresPath)
        {
            this(rm, structuresPath, Arrays.asList(
                new StructureBlockProcessor(),
                new AirBlockProcessor(),
                new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.GRAY_STAINED_GLASS))
            ));
        }

        public RegisterJigsawPieces(IReloadableResourceManager rm, String dir, List<StructureProcessor> processors)
        {
            try {
                String structurePath = STRUCTURES + "/" + dir;
                Map<String, Map<String, List<String>>> pieces = new HashMap<>();
                Collection<ResourceLocation> resources = rm.getAllResourceLocations(structurePath, file -> file.endsWith(".nbt"));

                for (ResourceLocation res : resources) {
                    String path, ruin;

                    String[] p = res.getPath().split("/");
                    if (p.length != 5) continue;
                    ruin = p[3];

                    if (!ruins.contains(ruin))
                        ruins.add(ruin);

                    path = res.getPath().replace(".nbt", "").replace(STRUCTURES + "/", "");

                    // create the ruin pieces tree structure for adding ruin piece types to
                    if (!pieces.containsKey(ruin))
                        pieces.put(ruin, new HashMap<>());

                    for (String pieceType : pieceTypes) {
                        if (!pieces.get(ruin).containsKey(pieceType))
                            pieces.get(ruin).put(pieceType, new ArrayList<>());

                        if (path.contains(pieceType.substring(0, pieceType.length() - 1)))
                            pieces.get(ruin).get(pieceType).add(path);
                    }
                }

                for (String ruin : pieces.keySet()) {
                    int size = 0;

                    for (String pieceType : pieceTypes) {
                        ResourceLocation patternId = new ResourceLocation(Strange.MOD_ID, dir + "/" + ruin + "/" + pieceType);

                        // add the starts for this ruin so it can be used when starting structure builds
                        if (pieceType.equals(STARTS))
                            starts.add(patternId);

                        // don't register same namespace with jigsaw manager twice
                        if (registeredPieces.contains(patternId))
                            continue;

                        List<String> piecesForType = pieces.get(ruin).get(pieceType);
                        List<Pair<JigsawPiece, Integer>> piecesAndWeights = new ArrayList<>();

                        // iterate ruin piecetypes to create a weighted jigsaw piece for each one
                        for (String piece : piecesForType) {
                            size = Math.max(size, UndergroundStructure.getWeight("_s", piece, 0));
                            SingleJigsawPiece jigsawPiece = new UndergroundJigsawPiece(Strange.MOD_ID + ":" + piece, processors);
                            piecesAndWeights.add(Pair.of(jigsawPiece, UndergroundStructure.getWeight("_w", piece, 1)));
                        }

                        // used to close off pathways
                        ResourceLocation end = hasEnds.contains(pieceType) ? new ResourceLocation(Strange.MOD_ID, dir + "/" + ruin + "/" + ENDS) : new ResourceLocation("empty");

                        if (piecesAndWeights.size() > 0) {
                            JigsawPattern pattern = new JigsawPattern(patternId, end, piecesAndWeights, JigsawPattern.PlacementBehaviour.RIGID);
                            JigsawManager.REGISTRY.register(pattern);
                        }
                        registeredPieces.add(patternId);
                    }

                    sizes.put(ruin, Math.max(2, size));
                }
            } catch (Exception e) {
                Meson.warn("[StructureHelper] exception loading structures", e);
            }
        }
    }
}
