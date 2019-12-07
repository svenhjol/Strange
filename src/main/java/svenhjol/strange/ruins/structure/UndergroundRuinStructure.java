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
                int cx = x >> 4;
                int cz = z >> 4;

                for (Structure<?> structure : UndergroundRuins.blacklist) {
                    for (int xx = cx - 10; xx <= x + 10; ++xx) {
                        for (int zz = cz - 10; zz <= z + 10; ++zz) {
                            if (structure.hasStartAt(gen, rand, xx, zz)) {
                                return true;
                            }
                        }
                    }
                }
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

            if (!UndergroundRuins.biomeRuins.containsKey(biomeCategory) || UndergroundRuins.biomeRuins.get(biomeCategory).isEmpty() || rand.nextFloat() < 0.15F) {
                biomeCategory = Biome.Category.NONE; // chance of being a general overworld structure
            }

            // TODO just for testing
//            biomeCategory = Biome.Category.SAVANNA;

            if (UndergroundRuins.biomeRuins.containsKey(biomeCategory) && !UndergroundRuins.biomeRuins.get(biomeCategory).isEmpty()) {
                Rotation rotation = Rotation.NONE;
                List<Direction> directions = Arrays.stream(Direction.values())
                    .filter(d -> d.getHorizontalIndex() >= 0)
                    .collect(Collectors.toList());


                // pick a direction
                Direction axis = directions.get(rand.nextInt(directions.size()));
                BlockPos offset = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                int cc = 1;

//                if (rand.nextFloat() < 0.25F) {
//                    cc = rand.nextInt(5) + 1;
//                }
                for (int c = 0; c < cc; c++) {
                    List<ResourceLocation> ruinTemplates = getRandomTemplates(biomeCategory, rand, rand.nextInt(4) + 1);
                    if (ruinTemplates.isEmpty()) continue;

                    ResourceLocation mainRes = ruinTemplates.get(0);
                    Template main = templates.getTemplateDefaulted(mainRes);

                    if (ruinTemplates.size() == 1) rotation = Rotation.randomRotation(rand);
                    UndergroundRuinPiece ruin = new UndergroundRuinPiece(templates, mainRes, offset, rotation, getDepth(mainRes));
                    components.add(ruin);

                    Collections.shuffle(directions);

                    BlockPos centrePos = ruin.getTemplatePosition();
                    Map<ResourceLocation, BlockPos> nextPieces = new HashMap<>();

                    for (int i = 1; i < ruinTemplates.size(); i++) {
                        Direction direction = directions.get(i - 1);
                        ResourceLocation nextRes = ruinTemplates.get(i);
                        Template next = templates.getTemplateDefaulted(nextRes);
                        int variation = 0;
                        int dist = 0;

                        if (direction == Direction.NORTH) {
                            dist = next.getSize().getZ() - 1;
                            nextPieces.put(nextRes, centrePos.north(dist).east(variation));
                        } else if (direction == Direction.EAST) {
                            dist = main.getSize().getX() - 1;
                            nextPieces.put(nextRes, centrePos.east(dist).north(variation));
                        } else if (direction == Direction.SOUTH) {
                            dist = main.getSize().getZ() - 1;
                            nextPieces.put(nextRes, centrePos.south(dist).east(variation));
                        } else if (direction == Direction.WEST) {
                            dist = next.getSize().getX() - 1;
                            nextPieces.put(nextRes, centrePos.west(dist).north(variation));
                        }

                        if (direction == axis) {
                            offset = offset.offset(direction, dist);
                        }
                    }

                    for (ResourceLocation r : nextPieces.keySet()) {
                        BlockPos p = nextPieces.get(r);
                        components.add(new UndergroundRuinPiece(templates, r, p, Rotation.NONE, getDepth(r)));
                    }

                    this.recalculateStructureSize();
                }
            }
        }

        public List<ResourceLocation> getRandomTemplates(Biome.Category biomeCategory, Random rand, int amount)
        {
            List<ResourceLocation> out = new ArrayList<>();

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
