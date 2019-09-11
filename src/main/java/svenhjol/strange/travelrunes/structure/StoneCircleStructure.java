package svenhjol.strange.travelrunes.structure;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.travelrunes.module.Runestones;
import svenhjol.strange.travelrunes.module.StoneCircles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

public class StoneCircleStructure extends ScatteredStructure<StoneCircleConfig>
{
    private static int tries = 64;
    public static IStructurePieceType SCP = StoneCirclePiece::new;

    public StoneCircleStructure(Function<Dynamic<?>, ? extends StoneCircleConfig> config)
    {
        super(config);
    }

    @Override
    public String getStructureName()
    {
        return "Stone_Circle";
    }

    @Override
    public int getSize()
    {
        return 1;
    }

    @Override
    public boolean hasStartAt(ChunkGenerator<?> gen, Random rand, int x, int z)
    {
        ChunkPos chunk = this.getStartPositionForPosition(gen, rand, x, z, 0, 0);

        if (x == chunk.x && z == chunk.z) {
            int px = x >> 4;
            int pz = z >> 4;

            rand.setSeed((long)(px ^ pz << 4) ^ gen.getSeed());
            rand.nextInt();

            if (rand.nextInt(2) > 0) return false;

            Biome biome = gen.getBiomeProvider().getBiome(new BlockPos((x << 4) + 9, 0, (z << 4) + 9));
            return gen.hasStructure(biome, StoneCircles.structure);
        }

        return false;
    }

    @Override
    protected int getSeedModifier()
    {
        return 247474720;
    }

    @Override
    public IStartFactory getStartFactory()
    {
        return StoneCircleStructure.Start::new;
    }

    public static class Start extends StructureStart
    {
        public Start(Structure<?> structure, int chunkX, int chunkZ, Biome biome, MutableBoundingBox bb, int ref, long seed)
        {
            super(structure, chunkX, chunkZ, biome, bb, ref, seed);
        }

        @Override
        public void init(ChunkGenerator<?> generator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn)
        {
            final StoneCirclePiece stoneCirclePiece = new StoneCirclePiece(this.rand, chunkX * 16, chunkZ * 16);
            this.components.add(stoneCirclePiece);
            this.recalculateStructureSize();
        }
    }

    public static class StoneCirclePiece extends ScatteredStructurePiece
    {
        public StoneCirclePiece(Random rand, int x, int z)
        {
            super(SCP, rand, x, 64, z, 16, 6, 16);
//            this.boundingBox = new MutableBoundingBox(pos.getX() - 16, pos.getY(), pos.getZ() - 16, pos.getX() + 16, pos.getY(), pos.getZ() + 16);
        }

        public StoneCirclePiece(TemplateManager templateManager, CompoundNBT tag)
        {
            super(SCP, tag);
        }

        @Override
        public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox bb, ChunkPos chunkPos)
        {
            boolean generated = false;
            int y = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, this.boundingBox.minX, this.boundingBox.minZ);
            BlockPos.MutableBlockPos pos;
            BlockPos surfacePos, surfacePosDown;

            if (world.getDimension().getType() == DimensionType.THE_NETHER) {

                for (int i = 20; i < 100; i++) {
                    for (int ii = 1; ii < tries; ii++) {
                        pos = new BlockPos.MutableBlockPos(this.boundingBox.minX, i, this.boundingBox.minZ);
                        surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                        surfacePosDown = surfacePos.down();

                        // try to generate a stone circle
                        if (world.isAirBlock(surfacePos)
                            && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.NETHERRACK)
                        ) {
                            int radius = rand.nextInt(4) + 5;
                            generated = generateCircle(world, pos, rand, radius, 0.1F, new ArrayList<>(Arrays.asList(
                                Blocks.NETHER_BRICKS.getDefaultState(),
                                Blocks.RED_NETHER_BRICKS.getDefaultState(),
                                Blocks.COAL_BLOCK.getDefaultState(),
                                Blocks.OBSIDIAN.getDefaultState()
                            )));
                        }

                        if (generated) break;
                    }
                }
            } else if (world.getDimension().getType() == DimensionType.THE_END) {

                for (int ii = 1; ii < tries; ii++) {
                    pos = new BlockPos.MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
                    surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                    surfacePosDown = surfacePos.down();

                    // try to generate a stone circle
                    if (world.isAirBlock(surfacePos)
                        && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.END_STONE)
                    ) {
                        int radius = rand.nextInt(7) + 4;
                        generated = generateCircle(world, pos, rand, radius, 0.1F, new ArrayList<>(Arrays.asList(
                            Blocks.OBSIDIAN.getDefaultState()
                        )));
                    }

                    if (generated) break;
                }
            } else {

                for (int ii = 1; ii < tries; ii++) {
                    pos = new BlockPos.MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
                    surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                    surfacePosDown = surfacePos.down();

                    // try to generate a stone circle
                    if ((world.isAirBlock(surfacePos) || world.hasWater(surfacePos))
                        && world.getBlockState(surfacePosDown).isSolid() && world.isSkyLightMax(surfacePosDown)
                    ) {
                        int radius = rand.nextInt(6) + 5;
                        generated = generateCircle(world, pos, rand, radius, 0.1F, new ArrayList<>(Arrays.asList(
                            Blocks.STONE.getDefaultState(),
                            Blocks.COBBLESTONE.getDefaultState(),
                            Blocks.MOSSY_COBBLESTONE.getDefaultState()
                        )));
                    }

                    if (generated) {
                        Meson.log("Generated at " + pos);
                        break;
                    }
                }
            }

            return generated;
        }

        public boolean generateCircle(IWorld world, BlockPos.MutableBlockPos pos, Random rand, int radius, float runeChance, ArrayList<BlockState> blocks)
        {
            boolean generated = false;
            boolean withRune = false;

            if (blocks.isEmpty()) {
                Meson.warn("You must pass blockstates to generate a circle");
                return false;
            }

            for (int i = 0; i < 360; i += 45)
            {
                int angle = i;
                double x1 = radius * Math.cos(angle * Math.PI / 180);
                double z1 = radius * Math.sin(angle * Math.PI / 180);

                for (int k = 5; k > -15; k--) {

                    BlockPos findPos = pos.add(x1, k, z1);
                    BlockPos findPosUp = findPos.up();
                    BlockState findState = world.getBlockState(findPos);
                    BlockState findStateUp = world.getBlockState(findPosUp);

                    if (findState.isSolid()
                        && findState.isOpaqueCube(world, findPos)
                        && (findStateUp.isAir() || world.hasWater(findPosUp))
                    ) {
                        boolean madeColumn = false;

                        int maxHeight = rand.nextInt(4) + 3;
                        world.setBlockState(findPos, blocks.get(0), 2);

                        for (int l = 1; l < maxHeight; l++) {
                            float f = rand.nextFloat();
                            BlockState setState = blocks.get(rand.nextInt(blocks.size()));

                            if (f < runeChance && Strange.loader.hasModule(Runestones.class)) {
                                setState = Runestones.getRune(world, pos, rand);
                                withRune = true;
                            }

                            world.setBlockState(findPos.up(l), setState, 2);
                            madeColumn = true;
                        }

                        if (madeColumn) {
                            generated = true;
                            break;
                        }
                    }
                }
            }

            if (withRune) {
                Meson.log("Generated with rune " + pos);
            }

            return generated;
        }

        public boolean generateInFortress()
        {
            return true;
        }
    }
}
