package svenhjol.strange.feature.stone_circles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.Fluids;
import svenhjol.charmony.base.Mods;
import svenhjol.charmony.helper.TagHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.feature.runestones.Runestones;

import java.util.List;
import java.util.function.Supplier;

public class StoneCirclePiece extends ScatteredFeaturePiece {
    static final String DEFINITION_TAG = "stone_circle_definition";
    private final IStoneCircleDefinition definition;

    public StoneCirclePiece(IStoneCircleDefinition definition, BlockPos startPos, RandomSource random) {
        super(StoneCircles.structurePiece.get(), startPos.getX(), startPos.getY(), startPos.getZ(), 16, 8, 16,
            getRandomHorizontalDirection(random));
        this.definition = definition;
    }

    public StoneCirclePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StoneCircles.structurePiece.get(), tag);
        this.definition = StoneCircles.DEFINITIONS.get(tag.getString(DEFINITION_TAG));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putString(DEFINITION_TAG, this.definition.getSerializedName());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {

        var log = Mods.common(Strange.ID).log();
        var jaggednessTolerance = definition.jaggednessTolerance();
        var maxJaggedness = jaggednessTolerance / 2; // If the surface Y value is this many blocks higher than starting Y, don't generate
        var minJaggedness = -(jaggednessTolerance / 2); // If the surface Y value is this many blocks lower than starting Y, don't generate
        var minRadius = definition.radius().getFirst();
        var maxRadius = definition.radius().getSecond();
        var radius = random.nextInt(maxRadius - minRadius) + minRadius;
        var minPillarHeight = definition.pillarHeight().getFirst();
        var maxPillarHeight = definition.pillarHeight().getSecond();
        var pillarBlocks = getPillarBlocks(level);
        var degrees = random.nextFloat() < 0.15f ? 45 + random.nextInt(5) - 10 : 45;
        var runestoneBlock = definition.runestoneBlock().map(Supplier::get).orElse(null);
        var generatedRunestones = 0;
        var maxRunestones = definition.maxRunestones();
        var runestoneChance = definition.runestoneChance();
        var generatedAnything = false;

        // Avoid stone circles being placed on the roof of the dimension.
        // Delegate to the definition to reposition the stone circle.
        if (level.dimensionType().hasCeiling()) {
            blockPos = definition.ceilingReposition(level, blockPos);
        }

        // Generate pillars in a rough circle.
        for (int i = 0; i < 360; i += degrees) {
            var x = (int)(radius * Math.cos(i * Math.PI / 180));
            var z = (int)(radius * Math.sin(i * Math.PI / 180));

            for (int s = maxJaggedness; s > minJaggedness; s--) {
                var checkPos = blockPos.offset(x, s, z);
                var checkUpPos = checkPos.above();
                var checkState = level.getBlockState(checkPos);
                var checkUpState = level.getBlockState(checkUpPos);

                var validSurfacePos = ((checkState.canOcclude() || checkState.getFluidState().is(Fluids.LAVA))
                    && (checkUpState.isAir() || !checkUpState.canOcclude() || level.isWaterAt(checkUpPos)));
                if (!validSurfacePos) {
                    continue;
                }

                var generatedColumn = false;
                var pillarHeight = random.nextInt(maxPillarHeight - minPillarHeight) + minPillarHeight;
                level.setBlock(checkPos, getRandomBlock(pillarBlocks, random), 2);

                for (int y = 1; y < pillarHeight; y++) {
                    var currentPos = checkPos.above(y);
                    var state = getRandomBlock(pillarBlocks, random);
                    var isTop = y == pillarHeight - 1;

                    if (isTop && runestoneBlock != null && generatedRunestones < maxRunestones && random.nextDouble() < runestoneChance) {
                        level.setBlock(currentPos, runestoneBlock.defaultBlockState(), 2);
                        Runestones.prepareRunestone(level, currentPos);
                        ++generatedRunestones;
                    } else {
                        level.setBlock(currentPos, state, 2);
                    }
                    generatedColumn = true;
                }

                if (generatedColumn) {
                    generatedAnything = true;
                    break;
                }
            }
        }

        if (!generatedAnything) {
            log.warn(getClass(), "Did not generate a stone circle at " + blockPos);
        }
    }

    private List<Block> getPillarBlocks(WorldGenLevel level) {
        var blocks = TagHelper.getValues(level.registryAccess().registryOrThrow(Registries.BLOCK), definition.pillarBlocks());

        if (blocks.isEmpty()) {
            throw new RuntimeException("No pillar blocks to generate stone circle");
        }

        return blocks;
    }

    private BlockState getRandomBlock(List<Block> blocks, RandomSource random) {
        return blocks.get(random.nextInt(blocks.size())).defaultBlockState();
    }
}
