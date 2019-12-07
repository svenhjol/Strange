package svenhjol.strange.ruins.structure;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.ScatteredStructure;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.strange.ruins.module.UndergroundRuins;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UndergroundRuinStructure extends ScatteredStructure<UndergroundRuinConfig>
{
    public static final int SEED_MODIFIER = 135318;
    public static final String GENERAL = "general";
    public static final String STRUCTURE_NAME = "Underground_Ruin";
    public static IStructurePieceType UNDERGROUND_RUIN_PIECE = UndergroundRuinPiece::new;

    public UndergroundRuinStructure(Function<Dynamic<?>, ? extends UndergroundRuinConfig> config)
    {
        super(config);
    }

    @Override
    public String getStructureName()
    {
        return STRUCTURE_NAME;
    }

    @Override
    public int getSize()
    {
        return 1;
    }

    @Override
    protected int getSeedModifier()
    {
        return SEED_MODIFIER;
    }

    @Override
    public boolean hasStartAt(ChunkGenerator<?> gen, Random rand, int x, int z)
    {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            Biome biome = gen.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9));

            // don't spawn underground ruin near blacklisted structure
            if (gen.hasStructure(biome, UndergroundRuins.structure)) {
//                int cx = x >> 4;
//                int cz = z >> 4;
//
//                for (Structure<?> structure : UndergroundRuins.blacklist) {
//                    for (int xx = cx - 10; xx <= x + 10; ++xx) {
//                        for (int zz = cz - 10; zz <= z + 10; ++zz) {
//                            if (structure.hasStartAt(gen, rand, xx, zz)) {
//                                return true;
//                            }
//                        }
//                    }
//                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected int getBiomeFeatureDistance(ChunkGenerator<?> gen)
    {
        return 4;
    }

    @Override
    protected int getBiomeFeatureSeparation(ChunkGenerator<?> gen)
    {
        return 3;
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return UndergroundRuinStructure.Start::new;
    }

    public static class Start extends StructureStart
    {
        public Start(Structure<?> structure, int chunkX, int chunkZ, Biome biome, MutableBoundingBox bb, int ref, long seed)
        {
            super(structure, chunkX, chunkZ, biome, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> gen, TemplateManager templates, int chunkX, int chunkZ, Biome biome)
        {
            Biome.Category biomeCategory = biome.getCategory();
            BlockPos pos = new BlockPos(chunkX * 16,  rand.nextInt(12) + 24, chunkZ * 16);

            if (pos.getY() == 0 || pos.getY() > 48) {
                pos = new BlockPos(pos.getX(), rand.nextInt(24) + 16, pos.getZ());
            }

            if (rand.nextFloat() < 0.15F || !UndergroundRuins.biomeRuins.containsKey(biomeCategory) || UndergroundRuins.biomeRuins.get(biomeCategory).isEmpty()) {
                biomeCategory = Biome.Category.NONE; // chance of being a general overworld structure
            }

            if (UndergroundRuins.biomeRuins.containsKey(biomeCategory) && !UndergroundRuins.biomeRuins.get(biomeCategory).isEmpty()) {
                Rotation rotation = Rotation.NONE;
                List<Direction> directions = Arrays.stream(Direction.values())
                    .filter(d -> d.getHorizontalIndex() >= 0)
                    .collect(Collectors.toList());

//                int numTemplates = 2;
                int numTemplates = rand.nextInt(4) + 1;

                List<ResourceLocation> ruinTemplates = getRandomTemplates(biomeCategory, rand, numTemplates);
                if (ruinTemplates.isEmpty()) return;

                // get the first template
                ResourceLocation mainRes = ruinTemplates.get(0);
                Template main = templates.getTemplateDefaulted(mainRes);
                BlockPos mainSize = main.getSize();

                // if there's only 1 template, rotate it randomly
                if (ruinTemplates.size() == 1) rotation = Rotation.randomRotation(rand);
                UndergroundRuinPiece mainRuin = new UndergroundRuinPiece(templates, mainRes, pos, rotation, getDepth(mainRes));
                components.add(mainRuin);

                // for any other other templates, place them at random directions from the centre template
                Collections.shuffle(directions);
                BlockPos centrePos = mainRuin.getTemplatePosition();
                Map<ResourceLocation, BlockPos> nextPieces = new HashMap<>();

                List<ResourceLocation> sublist = ruinTemplates.subList(1, ruinTemplates.size());
                int j = 0;

                while (j < sublist.size()) {
                    Direction direction = directions.get(j % 4);
                    ResourceLocation nextRes = sublist.get(j);
                    Template next = templates.getTemplateDefaulted(nextRes);
                    BlockPos nextSize = next.getSize();

                    int xc = (mainSize.getX() - nextSize.getX()) / 2;
                    int zc = (mainSize.getZ() - nextSize.getZ()) / 2;
                    int dist;

                    int xo = 0;
                    int zo = 0;

                    if (direction == Direction.NORTH) {
                        dist = next.getSize().getZ() - 1;
                        nextPieces.put(nextRes, centrePos.add(xc, 0, -dist));
                    } else if (direction == Direction.EAST) {
                        dist = main.getSize().getX() - 1;
                        nextPieces.put(nextRes, centrePos.add(dist, 0, zc));
                    } else if (direction == Direction.SOUTH) {
                        dist = main.getSize().getZ() - 1;
                        nextPieces.put(nextRes, centrePos.add(xc, 0, dist));
                    } else if (direction == Direction.WEST) {
                        dist = next.getSize().getX() - 1;
                        nextPieces.put(nextRes, centrePos.add(-dist, 0, zc));
                    }

//                    if (direction == Direction.NORTH) {
//                        xo = xc;
//                        zo = -(next.getSize().getZ() - 1);
//                    } else if (direction == Direction.EAST) {
//                        xo = main.getSize().getX() - 1;
//                        zo = zc;
//                    } else if (direction == Direction.SOUTH) {
//                        xo = xc;
//                        zo = main.getSize().getZ() - 1;
//                    } else if (direction == Direction.WEST) {
//                        xo = -(next.getSize().getX() - 1);
//                        zo = zc;
//                    }

//                    boolean cluster = rand.nextFloat() < 0.5F;
//                    if (cluster) {
//                        nextSize = templates.getTemplateDefaulted(sublist.get(++j)).getSize();
//                        xc = (mainSize.getX() - nextSize.getX()) / 2;
//                        zc = (mainSize.getZ() - nextSize.getZ()) / 2;
//                        nextPieces.put(next)
//                    }

                    j++;
                }

                for (ResourceLocation r : nextPieces.keySet()) {
                    components.add(new UndergroundRuinPiece(templates, r, nextPieces.get(r), Rotation.NONE, getDepth(r)));
                }

                this.recalculateStructureSize();
            }
        }

        public List<ResourceLocation> getRandomTemplates(Biome.Category biomeCategory, Random rand, int amount)
        {
            List<ResourceLocation> out = new ArrayList<>();

//            out = UndergroundRuins.biomeRuins.get(Biome.Category.NONE).get("bambi1").subList(0, 2);
//            return out;
//
            Map<String, List<ResourceLocation>> map = UndergroundRuins.biomeRuins.get(biomeCategory);
            if (map.keySet().size() == 0) return out;

            List<String> strings = new ArrayList<>(map.keySet());
            String set = strings.get(rand.nextInt(strings.size()));

            List<ResourceLocation> biomeTemplates = map.get(set);
            if (biomeTemplates.isEmpty()) return out;

            Collections.shuffle(biomeTemplates);
            return biomeTemplates.subList(0, Math.min(amount, biomeTemplates.size()));
        }

        public int getDepth(ResourceLocation res)
        {
            int depth = 0;
            String path = res.getPath();
            if (path.contains("depth")) {
                Pattern d = Pattern.compile("depth(\\d+)");
                Matcher m = d.matcher(path);
                if (m.find()) {
                    depth = Integer.parseInt(m.group(1));
                    return depth;
                }
            }

            return depth;
        }
    }
}
