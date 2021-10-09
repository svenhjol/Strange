package svenhjol.strange.module.stone_circles;

import com.mojang.serialization.Codec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import svenhjol.charm.enums.ICharmEnum;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class StoneCircleFeature extends StructureFeature<StoneCircleConfiguration> {
    public StoneCircleFeature(Codec<StoneCircleConfiguration> codec) {
        super(codec, StoneCircleFeature::generatePieces);
    }

    private static void generatePieces(StructurePiecesBuilder builder, StoneCircleConfiguration config, PieceGenerator.Context context) {
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        WorldgenRandom random = context.random();
        LevelHeightAccessor height = context.heightAccessor();
        ChunkPos chunkPos = context.chunkPos();

        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();
        int y = findSuitableY(config, chunkGenerator, height, random, x, z);

        builder.addPiece(new StoneCirclePiece(config.stoneCircleType, context, x, y, z));
    }

    private static int findSuitableY(StoneCircleConfiguration config, ChunkGenerator chunkGenerator, LevelHeightAccessor levelHeight, Random random, int x, int z) {
        int min = levelHeight.getMinBuildHeight() + 15;
        int y;

        if (config.stoneCircleType == Type.NETHER) {
            y = Mth.randomBetweenInclusive(random, 32, 100);
        } else {
            y = chunkGenerator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeight);
        }

        NoiseColumn column = chunkGenerator.getBaseColumn(x, z, levelHeight);
        Heightmap.Types heightMap = Heightmap.Types.WORLD_SURFACE_WG;

        int surface;
        for (surface = y; surface > min; --surface) {
            BlockState state = column.getBlock(y);
            if (heightMap.isOpaque().test(state)) {
                return surface;
            }
        }

        return surface;
    }

    public enum Type implements ICharmEnum {
        OVERWORLD("overworld"),
        NETHER("nether"),
        END("end");

        public static final Codec<StoneCircleFeature.Type> CODEC = StringRepresentable.fromEnum(StoneCircleFeature.Type::values, StoneCircleFeature.Type::byName);
        private static final Map<String, Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(Type::getName, type -> type));

        private final String name;

        Type(String type) {
            this.name = type;
        }

        public String getName() {
            return this.name;
        }

        public static StoneCircleFeature.Type byName(String name) {
            return BY_NAME.get(name);
        }
    }
}
