package svenhjol.strange.travelrunes.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.travelrunes.module.Runestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class StoneCircle
{
    private static int tries = 64;

    public static class Piece extends StructurePiece
    {
        public Piece(BlockPos pos)
        {
            super(IStructurePieceType.BTP, 0);
            this.boundingBox = new MutableBoundingBox(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }

        public Piece(TemplateManager templateManager, CompoundNBT tag)
        {
            super(IStructurePieceType.BTP, tag);
        }

        @Override
        protected void readAdditional(CompoundNBT tagCompound)
        {
            // do nothing I guess
        }

        @Override
        public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox bb, ChunkPos chunkPos)
        {
            boolean generated = false;
            int y = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, this.boundingBox.minX, this.boundingBox.minZ);
            MutableBlockPos pos;
            BlockPos surfacePos, surfacePosDown;

            if (world.getDimension().getType() == DimensionType.THE_NETHER) {

                for (int i = 20; i < 100; i++) {
                    for (int ii = 1; ii < tries; ii++) {
                        pos = new MutableBlockPos(this.boundingBox.minX, i, this.boundingBox.minZ);
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
                    pos = new MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
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
                    pos = new MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
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

        public boolean generateCircle(IWorld world, MutableBlockPos pos, Random rand, int radius, float runeChance, ArrayList<BlockState> blocks)
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
