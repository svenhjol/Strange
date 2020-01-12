package svenhjol.strange.runestones.structure;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.ScatteredStructurePiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import svenhjol.meson.Meson;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeLoot;
import svenhjol.strange.runestones.module.Runestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StoneCirclePiece extends ScatteredStructurePiece
{
    public static IStructurePieceType PIECE = StoneCirclePiece::new;
    public static final int TRIES = 64;

    public StoneCirclePiece(Random rand, BlockPos pos)
    {
        super(PIECE, rand, pos.getX(), 64, pos.getZ(), 16, 6, 16);
    }

    public StoneCirclePiece(TemplateManager templateManager, CompoundNBT tag)
    {
        super(PIECE, tag);
    }

    @Override
    public boolean addComponentParts(IWorld world, Random rand, MutableBoundingBox bb, ChunkPos chunkPos)
    {
        int y = world.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, this.boundingBox.minX, this.boundingBox.minZ);
        BlockPos.MutableBlockPos pos;
        BlockPos surfacePos, surfacePosDown;
        GenerationConfig config = new GenerationConfig();

        if (world.getDimension().getType() == DimensionType.THE_NETHER) {

            config.withChest = true;
            config.runeTries = 3;
            config.runeChance = 0.9F;
            config.radius = rand.nextInt(4) + 5;
            config.columnMinHeight = 3;
            config.columnVariation = 4;
            config.blocks = new ArrayList<>(Arrays.asList(
                Blocks.NETHER_BRICKS.getDefaultState()
            ));

            for (int i = 100; i > 20; i--) {
                for (int ii = 1; ii < TRIES; ii++) {
                    pos = new BlockPos.MutableBlockPos(this.boundingBox.minX, i, this.boundingBox.minZ);
                    surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                    surfacePosDown = surfacePos.down();

                    if (world.isAirBlock(surfacePos) && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.NETHERRACK)) {
                        return generateCircle(world, new BlockPos.MutableBlockPos(surfacePos), rand, config);
                    }
                }
            }

        } else if (world.getDimension().getType() == DimensionType.THE_END) {

            config.withChest = true;
            config.runeTries = 4;
            config.runeChance = 1.0F;
            config.radius = rand.nextInt(7) + 4;
            config.columnMinHeight = 3;
            config.columnVariation = 3;
            config.blocks = new ArrayList<>(Arrays.asList(
                Blocks.OBSIDIAN.getDefaultState()
            ));

            for (int ii = 1; ii < TRIES; ii++) {
                pos = new BlockPos.MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
                surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                surfacePosDown = surfacePos.down();

                if (world.isAirBlock(surfacePos) && world.getBlockState(surfacePosDown).getBlock().equals(Blocks.END_STONE)) {
                    return generateCircle(world, new BlockPos.MutableBlockPos(surfacePos), rand, config);
                }
            }

        } else {

            config.radius = rand.nextInt(6) + 5;
            config.runeTries = 2;
            config.runeChance = 0.8F;
            config.columnMinHeight = 4;
            config.columnVariation = 2;
            config.blocks = new ArrayList<>(Arrays.asList(
                Blocks.STONE.getDefaultState(),
                Blocks.COBBLESTONE.getDefaultState(),
                Blocks.MOSSY_COBBLESTONE.getDefaultState()
            ));

            for (int ii = 1; ii < TRIES; ii++) {
                pos = new BlockPos.MutableBlockPos(this.boundingBox.minX, y, this.boundingBox.minZ);
                surfacePos = pos.add(rand.nextInt(ii) - rand.nextInt(ii), 0, rand.nextInt(ii) - rand.nextInt(ii));
                surfacePosDown = surfacePos.down();

                if ((world.isAirBlock(surfacePos) || world.hasWater(surfacePos))
                    && world.getBlockState(surfacePosDown).isSolid() && world.isSkyLightMax(surfacePosDown)
                ) {
                    return generateCircle(world, new BlockPos.MutableBlockPos(surfacePos), rand, config);
                }
            }
        }

        return false;
    }

    public boolean generateCircle(IWorld world, BlockPos.MutableBlockPos pos, Random rand, GenerationConfig config)
    {
        boolean generated = false;
        boolean generatedWithRune = false;
        boolean runestonesEnabled = Strange.hasModule(Runestones.class);

        List<Integer> availableRunes = new ArrayList<>();
        if (runestonesEnabled) {
            for (int i = 0; i < Runestones.dests.size(); i++) {
                availableRunes.add(i);
            }

            if (availableRunes.size() == 0) {
                Meson.warn("No available runes to generate");
                return false;
            }
        }

        if (config.blocks.isEmpty()) {
            Meson.warn("You must pass blockstates to generate a circle");
            return false;
        }

        for (int i = 0; i < 360; i += 45)
        {
            double x1 = config.radius * Math.cos(i * Math.PI / 180);
            double z1 = config.radius * Math.sin(i * Math.PI / 180);

            for (int k = 5; k > -15; k--) {
                BlockPos findPos = pos.add(x1, k, z1);
                BlockPos findPosUp = findPos.up();
                BlockState findState = world.getBlockState(findPos);
                BlockState findStateUp = world.getBlockState(findPosUp);

                if ((findState.isSolid() || findState.getBlock() == Blocks.LAVA)
                    && (findStateUp.isAir(world, findPosUp) || world.hasWater(findPosUp))
                ) {
                    boolean madeColumn = false;

                    int maxHeight = rand.nextInt(config.columnVariation + 1) + config.columnMinHeight;
                    world.setBlockState(findPos, config.blocks.get(0), 2);

                    for (int l = 1; l < maxHeight; l++) {
                        BlockState state = config.blocks.get(rand.nextInt(config.blocks.size()));

                        if (runestonesEnabled && l == maxHeight - 1 && rand.nextFloat() < config.runeChance) {
                            for (int t = 0; t < config.runeTries; t++) {
                                int index = rand.nextInt(availableRunes.size());
                                int rune = availableRunes.get(index);

                                float f = rand.nextFloat();
                                float weight = Runestones.dests.get(index).weight;

                                if (f < weight) {
                                    availableRunes.remove(index);
                                    state = Runestones.getRunestoneBlock(world, rune);
                                    generatedWithRune = true;
                                    break;
                                }
                            }
                        }

                        world.setBlockState(findPos.up(l), state, 2);
                        madeColumn = true;
                    }

                    if (madeColumn) {
                        generated = true;
                        break;
                    }
                }
            }
        }

        if (config.withChest) {
            for (int k = 5; k > -15; k--) {
                BlockPos findPos = pos.add(0, k, 0);
                BlockPos findPosUp = findPos.up();
                BlockState findState = world.getBlockState(findPos);
                BlockState findStateUp = world.getBlockState(findPosUp);

                if (findState.isSolid() && findStateUp.isAir(world, findPosUp)) {
                    BlockState chest = Blocks.CHEST.getDefaultState();
                    world.setBlockState(findPosUp, chest, 2);
                    LockableLootTileEntity.setLootTable(world, rand, findPosUp, StrangeLoot.CHESTS_STONE_CIRCLE_TREASURE);
                    Meson.debug("Generated with chest " + pos);
                    break;
                }
            }
        }

        if (generatedWithRune) {
            Meson.debug("Generated with rune " + pos);
        }

        if (!generated) {
            Meson.debug("Did not generate");
        }

        return generated;
    }

    public static class GenerationConfig
    {
        public int radius = 4;
        public int columnMinHeight = 3;
        public int columnVariation = 3;
        public int runeTries = 1;
        public float runeChance = 0.8F;
        public boolean withChest = false;
        public List<BlockState> blocks = new ArrayList<>();
    }
}