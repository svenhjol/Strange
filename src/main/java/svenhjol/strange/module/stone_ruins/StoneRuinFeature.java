package svenhjol.strange.module.stone_ruins;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import svenhjol.strange.module.structures.RandomHeightJigsawFeature;

import java.util.Random;

public class StoneRuinFeature extends RandomHeightJigsawFeature {
    public StoneRuinFeature(Codec<JigsawConfiguration> codec, ResourceLocation starts, int size, int startY, int variation) {
        super(codec, starts, size, startY, variation, false, StoneRuinFeature::checkLocation, StoneRuinFeature::afterPlace);
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        return worldgenRandom.nextInt(7) >= 2;
    }

    private static void afterPlace(WorldGenLevel level, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
        var pieces = piecesContainer.pieces();
        if (pieces.isEmpty()) return;
        var start = pieces.get(0);
        var box = start.getBoundingBox();

        var rand = new Random(box.minX());
        var block1 = StoneRuins.surfaceBlocks.get(rand.nextInt(StoneRuins.surfaceBlocks.size()));
        var block2 = StoneRuins.surfaceBlocks.get(rand.nextInt(StoneRuins.surfaceBlocks.size()));
        var state1 = block1.defaultBlockState();
        var state2 = block2.defaultBlockState();

        var x = box.minX() + rand.nextInt(box.maxX() - box.minX());
        var z = box.minZ() + rand.nextInt(box.maxZ() - box.minZ());
        var pos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(x, 0, z));

        if (level.getBlockState(pos.below()) != state2) {
            level.setBlock(pos.below(), state1, 2);
            level.setBlock(pos, state2, 2);
        }
    }
}
