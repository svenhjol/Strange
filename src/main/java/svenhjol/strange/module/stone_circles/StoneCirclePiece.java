package svenhjol.strange.module.stone_circles;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.module.knowledge.Knowledge;
import svenhjol.strange.module.runestones.RunestoneBlockEntity;
import svenhjol.strange.module.runestones.Runestones;
import svenhjol.strange.module.runestones.enums.IRunestoneMaterial;
import svenhjol.strange.module.runestones.enums.RunestoneMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class StoneCirclePiece extends ScatteredFeaturePiece {
    public static int maxCheckSurface = 5; // if the surface Y value is this many blocks higher than starting Y, don't generate
    public static int minCheckSurface = -15; // if the surface Y value is this many blocks lower than starting Y, don't generate
    public static int maxRadius = 14; // max radius of the circle of stone pillars
    public static int minRadius = 5; // min radius of the circle of stone pillars
    public static int maxHeight = 8; // max possible height of a stone pillar
    public static int minHeight = 4; // min height of a stone pillar
    public static int maxRunestones = 5; // max number of runestones that will generate per circle

    private final StoneCircleFeature.Type stoneCircleType;

    public StoneCirclePiece(StoneCircleFeature.Type stoneCircleType, PieceGenerator.Context context, int x, int y, int z) {
        super(StoneCircles.STONE_CIRCLE_PIECE, x, y, z, 16, 8, 16, getRandomHorizontalDirection(context.random()));
        this.stoneCircleType = stoneCircleType;
    }

    public StoneCirclePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StoneCircles.STONE_CIRCLE_PIECE, tag);
        this.stoneCircleType = StoneCircleFeature.Type.byName(tag.getString("StoneCircleType"));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString("StoneCircleType", this.stoneCircleType.getName());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureFeatureManager structureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        int radius = random.nextInt(maxRadius - minRadius) + minRadius;
        IRunestoneMaterial material = null;
        List<BlockState> blocks = new ArrayList<>();

        if (Knowledge.getKnowledgeData().isEmpty()) {
            LogHelper.warn(this.getClass(), "Could not load KnowledgeData, giving up");
            return;
        }

        switch (stoneCircleType) {
            case OVERWORLD -> {
                material = RunestoneMaterial.STONE;
                blocks = Arrays.asList(
                    Blocks.STONE.defaultBlockState(),
                    Blocks.COBBLESTONE.defaultBlockState(),
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                );
            }
            case NETHER -> {
                material = RunestoneMaterial.BLACKSTONE;
                blocks = Arrays.asList(
                    Blocks.BLACKSTONE.defaultBlockState(),
                    Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                    Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                );
            }
            case END -> {
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

        int numberOfRunestonesGenerated = 0;
        for (int i = 0; i < 360; i += 45) {
            double x = radius * Math.cos(i * Math.PI / 180);
            double z = radius * Math.sin(i * Math.PI / 180);

            for (int s = maxCheckSurface; s > minCheckSurface; s--) {
                BlockPos checkPos = blockPos.offset(x, s, z);
                BlockPos checkUpPos = checkPos.above();
                BlockState checkState = level.getBlockState(checkPos);
                BlockState checkUpState = level.getBlockState(checkUpPos);

                boolean validSurfacePos = ((checkState.canOcclude() || checkState.getBlock() == Blocks.LAVA)
                    && (checkUpState.isAir() || !checkUpState.canOcclude() || level.isWaterAt(checkUpPos)));

                if (!validSurfacePos)
                    continue;

                boolean generatedColumn = false;
                int height = random.nextInt(maxHeight - minHeight) + minHeight;
                level.setBlock(checkPos, blocks.get(0), 2);

                for (int y = 1; y < height; y++) {
                    BlockPos currentPos = checkPos.above(y);
                    BlockState state = blocks.get(random.nextInt(blocks.size()));
                    boolean isTop = y == height - 1;
                    float difficulty = 0F;
                    ResourceLocation location = null;

                    if (isTop) {
                        // always try and generate a spawn rune first
                        if (!generatedSpawnRune) {
                            state = Runestones.RUNESTONE_BLOCKS.get(material).defaultBlockState();
                            location = Runestones.SPAWN;
                            generatedSpawnRune = true;

                        } else if (numberOfRunestonesGenerated < maxRunestones && random.nextFloat() < 0.75F) {
                            // scale difficulty according to number of runes added
                            difficulty += ((numberOfRunestonesGenerated + 1) * (random.nextFloat() * (1F / maxRunestones)));

                            // scale difficulty according to distance from spawn
                            double distX = Math.min(1.0D, Math.abs(currentPos.getX()) / 1000000D);
                            double distZ = Math.min(1.0D, Math.abs(currentPos.getZ()) / 1000000D);
                            difficulty += random.nextFloat() * (Math.max(distX, distZ) * 0.8F);

                            state = Runestones.RUNESTONE_BLOCKS.get(material).defaultBlockState();
                            ++numberOfRunestonesGenerated;
                        }
                    }

                    level.setBlock(currentPos, state, 2);

                    BlockEntity blockEntity = level.getBlockEntity(currentPos);
                    if (blockEntity instanceof RunestoneBlockEntity runestone) {
                        runestone.difficulty = Math.max(0.0F, Math.min(1.0F, difficulty));
                        if (location != null) {
                            runestone.location = location;
                        }
                        LogHelper.debug(this.getClass(), "Created runestone with difficulty: " + difficulty);
                    }
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
