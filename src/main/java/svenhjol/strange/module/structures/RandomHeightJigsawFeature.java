package svenhjol.strange.module.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @see {{@link net.minecraft.world.level.levelgen.feature.JigsawFeature}}
 */
public class RandomHeightJigsawFeature extends NoiseAffectingStructureFeature<JigsawConfiguration> {
    public RandomHeightJigsawFeature(Codec<JigsawConfiguration> codec, int structureStartY, int variation, boolean relativeToBottom, Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> predicate) {
        super(codec, context -> {
            if (!predicate.test(context)) {
                return Optional.empty();
            }

            WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(1441L));
            worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
            int r = variation > 0 ? worldgenRandom.nextInt(variation * 2) : 0;
            int y;

            if (relativeToBottom) {
                y = context.chunkGenerator().getMinY() + structureStartY + (variation - r);
            } else {
                y = structureStartY + (variation - r);
            }

            BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), y, context.chunkPos().getMinBlockZ());
            return JigsawPlacement.addPieces(context, PoolElementStructurePiece::new, blockPos, false, false);
        });
    }
}
