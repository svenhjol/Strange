package svenhjol.strange.feature.stone_circles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class StoneCircleStructure extends Structure {
    public static final Codec<StoneCircleStructure> CODEC = RecordCodecBuilder.<StoneCircleStructure>mapCodec(
        instance -> instance.group(
            StoneCircleStructure.settingsCodec(instance),
            StoneCircles.definitionsCodec.fieldOf("stone_circle_definition")
                .forGetter(structure -> structure.definition))
                .apply(instance, StoneCircleStructure::new)).codec();

    private final IStoneCircleDefinition definition;

    protected StoneCircleStructure(StructureSettings settings, IStoneCircleDefinition definition) {
        super(settings);

        var defaultDefinition = StoneCircles.DEFINITIONS.get(StoneCircleDefinitions.DEFAULT);
        if (defaultDefinition == null) {
            throw new RuntimeException("Missing default stone circle definition");
        }

        this.definition = StoneCircles.DEFINITIONS.getOrDefault(definition.getSerializedName(), defaultDefinition);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        var chunkGenerator = context.chunkGenerator();
        var height = context.heightAccessor();
        var chunkPos = context.chunkPos();
        var x = chunkPos.getMinBlockX();
        var z = chunkPos.getMinBlockZ();
        var randomSource = RandomSource.create(x + z);
        var randomState = context.randomState();

        var y = findSuitableY(chunkGenerator, height, randomSource, randomState, x, z);
        if (y == Integer.MIN_VALUE) {
            return Optional.empty();
        }

        var holder = chunkGenerator.getBiomeSource().getNoiseBiome(
            QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), randomState.sampler());

        if (!context.validBiome().test(holder)) {
            return Optional.empty();
        }

        var startPos = new BlockPos(x, y, z);
        return Optional.of(new GenerationStub(startPos,
            builder -> builder.addPiece(new StoneCirclePiece(definition, startPos, context.random()))));
    }

    private int findSuitableY(ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeight, RandomSource random, RandomState randomState, int x, int z) {
        var min = levelHeight.getMinBuildHeight() + 15;
        int y;

        y = chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeight, randomState);

        var column = chunkGenerator.getBaseColumn(x, z, levelHeight, randomState);
        var heightMap = Heightmap.Types.WORLD_SURFACE_WG;

//        if (hasCeiling) {
//            y = Mth.randomBetweenInclusive(random, y - 80, y - 40);
//        }

        int surface;
        for (surface = y; surface > min; --surface) {
            var state = column.getBlock(y);
            var above = column.getBlock(y + 1);
            if (heightMap.isOpaque().test(state) && (!heightMap.isOpaque().test(above))) {
                return surface;
            }
        }

        return Integer.MIN_VALUE;
    }

    @Override
    public StructureType<?> type() {
        return StoneCircles.structureType.get();
    }
}
