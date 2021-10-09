package svenhjol.strange.module.stone_circles;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.runestones.Runestones;
import svenhjol.strange.module.runestones.destination.BaseDestination;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class StoneCirclePiece extends ScatteredFeaturePiece {
    public static int maxCheckSurface = 5; // if the surface Y value is this many blocks higher than starting Y, don't generate
    public static int minCheckSurface = -15; // if the surface Y value is this many blocks lower than starting Y, don't generate
    public static int maxRadius = 14; // max radius of the circle of stone pillars
    public static int minRadius = 5; // min radius of the circle of stone pillars
    public static int maxHeight = 8; // max possible height of a stone pillar
    public static int minHeight = 4; // min height of a stone pillar
    public static int maxRunestones = 5; // max number of runestones that will generate per circle
    public static int runestoneTries = 10; // number of attempts at runestone placement from available runestones

    private final StoneCircleFeature.Type stoneCircleType;

    public StoneCirclePiece(StoneCircleFeature.Type stoneCircleType, PieceGenerator.Context context, int x, int y, int z) {
        super(StoneCircles.STONE_CIRCLE_PIECE, x, y, z, 16, 8, 16, getRandomHorizontalDirection(context.random()));
        this.stoneCircleType = stoneCircleType;
    }

    public StoneCirclePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StoneCircles.STONE_CIRCLE_PIECE, tag);
        DataResult<StoneCircleConfiguration> stoneCircleType = StoneCircleConfiguration.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, tag.get("stone_circle_type")));
        this.stoneCircleType = stoneCircleType.getOrThrow(true, err -> LogHelper.error(this.getClass(), err)).stoneCircleType;
    }

    @Override
    public void postProcess(WorldGenLevel world, StructureFeatureManager structureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int radius = random.nextInt(maxRadius - minRadius) + minRadius;
        IRunestoneMaterial material = null;
        ResourceLocation dimension = null;
        List<BlockState> blocks = new ArrayList<>();

        switch (stoneCircleType) {
            case OVERWORLD -> {
                dimension = ServerLevel.OVERWORLD.location();
                material = RunestoneMaterial.STONE;
                blocks = Arrays.asList(
                    Blocks.STONE.defaultBlockState(),
                    Blocks.COBBLESTONE.defaultBlockState(),
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                );
            }
            case NETHER -> {
                dimension = ServerLevel.NETHER.location();
                material = RunestoneMaterial.BLACKSTONE;
                blocks = Arrays.asList(
                    Blocks.BLACKSTONE.defaultBlockState(),
                    Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                    Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                );
            }
            case END -> {
                dimension = ServerLevel.END.location();
                material = RunestoneMaterial.OBSIDIAN;
                blocks = Arrays.asList(
                    Blocks.OBSIDIAN.defaultBlockState(),
                    Blocks.CRYING_OBSIDIAN.defaultBlockState()
                );
            }
        }

        // generate the circle
        boolean generatedSomething = false;
        boolean generatedSpawnRune = false;

        List<BaseDestination> destinations = Runestones.AVAILABLE_DESTINATIONS.get(dimension);

        if (destinations.isEmpty()) {
            LogHelper.warn(this.getClass(), "There are no available runestone destinations for this dimension, giving up");
            return;
        }

        int numberOfRunestonesGenerated = 0;
        for (int i = 0; i < 360; i += 45) {
            double x = radius * Math.cos(i * Math.PI / 180);
            double z = radius * Math.sin(i * Math.PI / 180);

            for (int s = maxCheckSurface; s > minCheckSurface; s--) {
                BlockPos checkPos = blockPos.offset(x, s, z);
                BlockPos checkUpPos = checkPos.above();
                BlockState checkState = world.getBlockState(checkPos);
                BlockState checkUpState = world.getBlockState(checkUpPos);

                boolean validSurfacePos = ((checkState.canOcclude() || checkState.getBlock() == Blocks.LAVA)
                    && (checkUpState.isAir() || !checkUpState.canOcclude() || world.isWaterAt(checkUpPos)));

                if (!validSurfacePos)
                    continue;

                boolean generatedColumn = false;
                int height = random.nextInt(maxHeight - minHeight) + minHeight;
                world.setBlock(checkPos, blocks.get(0), 2);


                for (int y = 1; y < height; y++) {
                    BlockState state = blocks.get(random.nextInt(blocks.size()));
                    boolean isTop = y == height - 1;

                    if (isTop) {
                        // always try and generate a spawn rune first
                        if (!generatedSpawnRune) {
                            // TODO: set the runestone properties here
                            state = Runestones.RUNESTONE_BLOCKS.get(material).defaultBlockState();
                            generatedSpawnRune = true;
                        } else if (numberOfRunestonesGenerated < maxRunestones && random.nextFloat() < 0.3F) {

                            // Try and generate a runestone. Replace the state with the runestone if successful
                            for (int tries = 0; tries < runestoneTries; tries++) {
                                float f = random.nextFloat();
                                List<BaseDestination> matching = destinations.stream().filter(d -> f < d.getWeight()).collect(Collectors.toList());
                                if (matching.isEmpty()) continue;

                                BaseDestination destination = matching.get(random.nextInt(matching.size()));
                                // TODO set the runestone properties here
                                state = Runestones.RUNESTONE_BLOCKS.get(material).defaultBlockState();
                                ++numberOfRunestonesGenerated;
                                break;
                            }
                        }
                    }

                    world.setBlock(checkPos.above(y), state, 2);
                    generatedColumn = true;
                }

                if (generatedColumn) {
                    generatedSomething = true;
                    break;
                }
            }
        }

        if (!generatedSomething) {
            LogHelper.debug(this.getClass(), "Did not generate a stone circle at: " + blockPos);
        }
    }
}
