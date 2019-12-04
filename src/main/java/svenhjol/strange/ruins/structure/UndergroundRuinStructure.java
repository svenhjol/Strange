package svenhjol.strange.ruins.structure;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
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

//    @Override
//    public boolean hasStartAt(ChunkGenerator<?> gen, Random rand, int x, int z)
//    {
//        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);
//
//        if (x == chunk.x && z == chunk.z) {
//            Biome biome = gen.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9));
//            return gen.hasStructure(biome, UndergroundRuins.structure);
//        }
//
//        return false;
//    }

    @Override
    protected int getBiomeFeatureDistance(ChunkGenerator<?> gen)
    {
        return 8;
    }

    @Override
    protected int getBiomeFeatureSeparation(ChunkGenerator<?> gen)
    {
        return 4;
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
                Rotation rotation = Rotation.randomRotation(rand);
                int numStructures = rand.nextInt(4) + 1;

                List<ResourceLocation> ruinTemplates = getRandomTemplates(biomeCategory, rand, numStructures);
                if (ruinTemplates.isEmpty()) return;

                ResourceLocation mainRes = ruinTemplates.get(0);
                Template main = templates.getTemplateDefaulted(mainRes);
                UndergroundRuinPiece ruin = new UndergroundRuinPiece(templates, mainRes, pos, rotation, ruinTemplates.size() == 1);
                components.add(ruin);

                List<Direction> directions = Arrays.stream(Direction.values())
                    .filter(d -> d.getHorizontalIndex() >= 0)
                    .collect(Collectors.toList());

                Collections.shuffle(directions);

                BlockPos centrePos = ruin.getTemplatePosition();

                for (int i = 1; i < ruinTemplates.size(); i++) {
                    BlockPos nextPos = null;
                    Direction direction = directions.get(i);
                    ResourceLocation nextRes = ruinTemplates.get(i);
                    Template next = templates.getTemplateDefaulted(nextRes);

                    int offset = rand.nextInt(7) - 2;

                    if (direction == Direction.NORTH) {
                        nextPos = centrePos.north(next.getSize().getZ()).east(offset);
                    } else if (direction == Direction.EAST) {
                        nextPos = centrePos.east(main.getSize().getX()).north(offset);
                    } else if (direction == Direction.SOUTH) {
                        nextPos = centrePos.south(main.getSize().getZ()).east(offset);
                    } else if (direction == Direction.WEST) {
                        nextPos = centrePos.west(next.getSize().getX()).north(offset);
                    }
//                    } else if (direction == Direction.DOWN) {
//                        if (centrePos.add(0, -next.getSize().getY(), 0).getY() > 10) {
//                            nextPos = centrePos.add(rand.nextInt(5) - 2, -next.getSize().getY(), rand.nextInt(5) - 2);
//                        }
//                    } else if (direction == Direction.UP) {
//                        if (centrePos.add(0, main.getSize().getY(), 0).getY() < 48) {
//                            nextPos = centrePos.add(rand.nextInt(5) - 2, main.getSize().getY(), rand.nextInt(5) - 2);
//                        }
//                    }

                    if (nextPos != null) {
                        nextPos.rotate(rotation);
                        components.add(new UndergroundRuinPiece(templates, nextRes, nextPos, rotation, false));
                    }
                }

                this.recalculateStructureSize();
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
    }
}
