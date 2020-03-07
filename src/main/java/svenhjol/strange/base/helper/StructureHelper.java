package svenhjol.strange.base.helper;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Blocks;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import svenhjol.strange.Strange;
import svenhjol.strange.base.feature.DecorationProcessor;
import svenhjol.strange.base.feature.StrangeJigsawPiece;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructureHelper {
    public static final List<ResourceLocation> registeredPieces = new ArrayList<>();

    public static int getWeight(String prefix, String name, int def) {
        if (name.contains(prefix)) {
            Pattern p = Pattern.compile(prefix + "(\\d+)");
            Matcher m = p.matcher(name);
            if (m.find()) return Integer.parseInt(m.group(1));
        }
        return def;
    }

    public static class RegisterJigsawPieces {
        public static final String STRUCTURES = "structures";
        public static final String CORRIDORS = "corridors";
        public static final String ROOMS = "rooms";
        public static final String SECRETS = "secrets";
        public static final String AREAS = "areas";
        public static final String STARTS = "starts";
        public static final String ENDS = "ends";

        public final List<String> structures = new ArrayList<>();
        public final List<ResourceLocation> starts = new ArrayList<>();
        public final Map<String, Integer> sizes = new HashMap<>();

        protected final List<String> pieceTypes = Arrays.asList(CORRIDORS, ROOMS, SECRETS, AREAS, STARTS, ENDS);
        protected final List<String> hasEnds = Arrays.asList(CORRIDORS, ROOMS);

        public RegisterJigsawPieces(IReloadableResourceManager rm, String structuresPath) {
            this(rm, structuresPath, Arrays.asList(
                new DecorationProcessor(),
                new BlockIgnoreStructureProcessor(ImmutableList.of(Blocks.GRAY_STAINED_GLASS))
            ));
        }

        public RegisterJigsawPieces(IReloadableResourceManager rm, String dir, List<StructureProcessor> processors) {
            try {
                String structurePath = STRUCTURES + "/" + dir;
                Map<String, Map<String, List<String>>> pieces = new HashMap<>();
                Collection<ResourceLocation> resources = rm.getAllResourceLocations(structurePath, file -> file.endsWith(".nbt"));

                for (ResourceLocation res : resources) {
                    String path, structure;

                    String[] p = res.getPath().split("/");
                    structure = p[p.length - 2];

                    if (!structures.contains(structure))
                        structures.add(structure);

                    path = res.getPath().replace(".nbt", "").replace(STRUCTURES + "/", "");

                    // create the structure pieces tree for adding piece types to
                    if (!pieces.containsKey(structure))
                        pieces.put(structure, new HashMap<>());

                    for (String pieceType : pieceTypes) {
                        if (!pieces.get(structure).containsKey(pieceType))
                            pieces.get(structure).put(pieceType, new ArrayList<>());

                        if (path.contains(pieceType.substring(0, pieceType.length() - 1)))
                            pieces.get(structure).get(pieceType).add(path);
                    }
                }

                for (String structure : pieces.keySet()) {
                    int size = 0;

                    for (String pieceType : pieceTypes) {
                        String path = dir.equals(structure) ? structure : dir + "/" + structure;
                        ResourceLocation patternId = new ResourceLocation(Strange.MOD_ID, path + "/" + pieceType);

                        // add the starts for this structure so it can be used when starting builds
                        if (pieceType.equals(STARTS))
                            starts.add(patternId);

                        // don't register same namespace with jigsaw manager twice
                        if (registeredPieces.contains(patternId))
                            continue;

                        List<String> piecesForType = pieces.get(structure).get(pieceType);
                        List<Pair<JigsawPiece, Integer>> piecesAndWeights = new ArrayList<>();

                        // iterate structure piecetypes to create a weighted jigsaw piece for each one
                        for (String piece : piecesForType) {
                            size = Math.max(size, getWeight("_s", piece, 0));
                            StrangeJigsawPiece jigsawPiece = new StrangeJigsawPiece(Strange.MOD_ID + ":" + piece, processors);
                            piecesAndWeights.add(Pair.of(jigsawPiece, getWeight("_w", piece, 1)));
                        }

                        // used to close off pathways
                        ResourceLocation end = hasEnds.contains(pieceType) ? new ResourceLocation(Strange.MOD_ID, path + "/" + ENDS) : new ResourceLocation("empty");

                        if (piecesAndWeights.size() > 0) {
                            JigsawPattern pattern = new JigsawPattern(patternId, end, piecesAndWeights, JigsawPattern.PlacementBehaviour.RIGID);
                            JigsawManager.REGISTRY.register(pattern);
                        }
                        registeredPieces.add(patternId);
                    }

                    sizes.put(structure, Math.max(2, size));
                }
            } catch (Exception e) {
                Strange.LOG.error("Exception loading structures: " + e);
            }
        }
    }
}
