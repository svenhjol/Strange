package svenhjol.strange.ruins.structure;

import net.minecraft.block.Block;
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
import net.minecraft.world.gen.feature.structure.ScatteredStructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.strange.runestones.module.Runestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MarkerPiece extends ScatteredStructurePiece
{
    public static IStructurePieceType PIECE = MarkerPiece::new;
    public static final int TRIES = 64;

    public MarkerPiece(Random rand, BlockPos pos)
    {
        super(PIECE, rand, pos.getX(), 64, pos.getZ(), 1, 2, 2);
    }

    public MarkerPiece(TemplateManager templateManager, CompoundNBT tag)
    {
        super(PIECE, tag);
    }

    @Override
    public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox bb, ChunkPos chunk)
    {
        BlockPos foundPos = null;
        DimensionType dim = world.getDimension().getType();
        int x = this.boundingBox.minX;
        int z = this.boundingBox.minZ;

        if (dim == DimensionType.THE_NETHER) {

            for (int i = 100; i > 32; i--) {
                for (int ii = 1; ii < TRIES; ii++) {
                    MutableBlockPos pos = new MutableBlockPos(x, i, z);
                    BlockPos surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                    BlockPos surfacePosDown = surfacePos.down();

                    if (world.isAirBlock(surfacePos) && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.NETHERRACK)) {
                        foundPos = surfacePos;
                        break;
                    }
                }
            }

        } else if (dim == DimensionType.THE_END) {

            int y = world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, x, z);

            for (int i = 1; i < TRIES; i++) {
                MutableBlockPos pos = new MutableBlockPos(x, y, z);
                BlockPos surfacePos = pos.add(rand.nextInt(i) - rand.nextInt(i), 0, rand.nextInt(i) - rand.nextInt(i));
                BlockPos surfacePosDown = surfacePos.down();

                if (world.isAirBlock(surfacePos) && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.END_STONE)) {
                    foundPos = surfacePos;
                    break;
                }
            }

        } else {

            int y = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, x, z);

            for (int i = 1; i < TRIES; i++) {
                MutableBlockPos pos = new MutableBlockPos(x, y, z);
                BlockPos surfacePos = pos.add(rand.nextInt(i) - rand.nextInt(i), 0, rand.nextInt(i) - rand.nextInt(i));
                BlockPos surfacePosDown = surfacePos.down();

                if ((world.isAirBlock(surfacePos) || world.hasWater(surfacePos))
                    && world.getBlockState(surfacePosDown).isSolid()
                    && world.isSkyLightMax(surfacePosDown)
                ) {
                    foundPos = surfacePos;
                    break;
                }
            }
        }

        if (foundPos != null)
            return generateMarker(world, dim, foundPos, rand);

        return false;
    }

    private boolean generateMarker(IWorld world, DimensionType dim, BlockPos pos, Random rand)
    {
        List<Block> blocks = new ArrayList<>();

        if (dim == DimensionType.OVERWORLD) {
            blocks.addAll(Arrays.asList(Blocks.CHISELED_STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.MOSSY_COBBLESTONE, Blocks.MOSSY_STONE_BRICKS));
        } else if (dim == DimensionType.THE_NETHER) {
            blocks.addAll(Arrays.asList(Blocks.NETHER_BRICKS, Blocks.OBSIDIAN));
        } else if (dim == DimensionType.THE_END) {
            blocks.add(Blocks.OBSIDIAN);
        }

        if (blocks.isEmpty())
            return false;

        world.setBlockState(pos, blocks.get(rand.nextInt(blocks.size())).getDefaultState(), 2);

        if (Meson.isModuleEnabled("strange:runestones") && rand.nextFloat() < 0.25F)
            world.setBlockState(pos.up(), Runestones.getRandomBlock(dim), 2);

        return true;
    }
}
