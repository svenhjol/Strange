package svenhjol.strange.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePieceWithDimensions;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import svenhjol.meson.Meson;
import svenhjol.strange.module.Runestones;
import svenhjol.strange.module.StoneCircles;

import java.util.*;

public class StoneCircleGenerator extends StructurePieceWithDimensions {
    public StoneCircleGenerator(Random random, BlockPos pos) {
        super(StoneCircles.STONE_CIRCLE_PIECE, random, pos.getX(), 64, pos.getZ(), 16, 8, 16);
    }

    public StoneCircleGenerator(StructureManager structureManager, CompoundTag tag) {
        super(StoneCircles.STONE_CIRCLE_PIECE, tag);
    }

    @Override
    public boolean generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator gen, Random random, BlockBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int radius = random.nextInt(7) + 5;
        int minHeight = 4;
        int maxHeight = 8;
        int runeTries = 10;
        float runeChance = 0.8F;
        String lootTable = "";

        List<BlockState> blocks = new ArrayList<>(Arrays.asList(
            Blocks.STONE.getDefaultState(),
            Blocks.COBBLESTONE.getDefaultState(),
            Blocks.MOSSY_COBBLESTONE.getDefaultState()
        ));

        // generate the circle
        boolean generatedSomething = false;
        Map<Integer, Float> availableRunes = new HashMap<>();

        for (int i = 0; i < Runestones.availableDestinations.size(); i++) {
            availableRunes.put(i, Runestones.availableDestinations.get(i).getWeight());
        }

        if (availableRunes.size() == 0) {
            Meson.LOG.warn("No available runes to generate");
            return false;
        }

        for (int i = 0; i < 360; i += 45) {
            double x = radius * Math.cos(i * Math.PI / 180);
            double z = radius * Math.sin(i * Math.PI / 180);

            for (int s = 5; s > -15; s--) {
                BlockPos checkPos = blockPos.add(x, s, z);
                BlockPos checkUpPos = checkPos.up();
                BlockState checkState = world.getBlockState(checkPos);
                BlockState checkUpState = world.getBlockState(checkUpPos);

                boolean validSurfacePos = ((checkState.isOpaque() || checkState.getBlock() == Blocks.LAVA)
                    && (checkUpState.isAir() || world.isWater(checkUpPos)));

                if (!validSurfacePos)
                    continue;

                boolean generatedColumn = false;
                int height = random.nextInt(maxHeight - minHeight) + minHeight;
                world.setBlockState(checkPos, blocks.get(0), 2);

                for (int y = 1; y < height; y++) {
                    BlockState state = blocks.get(random.nextInt(blocks.size()));

                    boolean isTop = y == height - 1;
                    if (isTop && random.nextFloat() < runeChance) {

                        // Try and generate a rune. Replace the state with the runestone if successful
                        for (int tries = 0; tries < runeTries; tries++) {
                            List<Integer> keys = new ArrayList<>(availableRunes.keySet());
                            int rune = keys.get(random.nextInt(keys.size()));

                            float f = random.nextFloat();
                            float weight = availableRunes.get(rune);

                            if (f >= weight)
                                continue;

                            availableRunes.remove(rune);
                            state = Runestones.RUNESTONE_BLOCKS.get(rune).getDefaultState();
                            break;
                        }
                    }

                    world.setBlockState(checkPos.up(y), state, 2);
                    generatedColumn = true;
                }

                if (generatedColumn) {
                    generatedSomething = true;
                    break;
                }
            }
        }

        // TODO spawn chest at center

        if (!generatedSomething)
            Meson.LOG.debug("Did not generate a stone circle at: " + blockPos);

        return generatedSomething;
    }
}
