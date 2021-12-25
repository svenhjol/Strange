package svenhjol.strange.module.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import svenhjol.charm.helper.LogHelper;
import svenhjol.strange.Strange;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * {@link net.minecraft.world.level.levelgen.feature.JigsawFeature}
 */
public abstract class RandomHeightJigsawFeature extends NoiseAffectingStructureFeature<JigsawConfiguration> {
    public RandomHeightJigsawFeature(Codec<JigsawConfiguration> codec, ResourceLocation starts, int size, int startY, int variation, boolean relativeToBottom, Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> checkLocation) {
        super(codec, context -> {
            if (!checkLocation.test(context)) {
                return Optional.empty();
            }

            WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(1441L));
            worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
            int r = variation > 0 ? worldgenRandom.nextInt(variation * 2) : 0;
            int y;

            if (relativeToBottom) {
                y = context.chunkGenerator().getMinY() + startY + (variation - r);
            } else {
                y = startY + (variation - r);
            }

            // adjust for air/water
            BlockPos middle = context.chunkPos().getMiddleBlockPosition(0);
            NoiseColumn noiseColumn = context.chunkGenerator().getBaseColumn(middle.getX(), middle.getZ(), context.heightAccessor());

            int tries = 0;
            do {
                BlockState block = noiseColumn.getBlock(y - tries);
                if (block.getFluidState().isEmpty() && !block.isAir()) break;
            } while (tries++ < variation);
            if (tries == 10) return Optional.empty();

            BlockPos blockPos = new BlockPos(context.chunkPos().getMinBlockX(), y, context.chunkPos().getMinBlockZ());

            JigsawConfiguration newConfig = new JigsawConfiguration(
                () -> context.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY)
                    .get(starts), size);

            PieceGeneratorSupplier.Context<JigsawConfiguration> newContext = new PieceGeneratorSupplier.Context<>(
                context.chunkGenerator(),
                context.biomeSource(),
                context.seed(),
                context.chunkPos(),
                newConfig,
                context.heightAccessor(),
                context.validBiome(),
                context.structureManager(),
                context.registryAccess()
            );

            Optional<PieceGenerator<JigsawConfiguration>> generator = JigsawPlacement.addPieces(newContext, PoolElementStructurePiece::new, blockPos, false, false);

            if (generator.isPresent()) {
                LogHelper.debug(Strange.MOD_ID, RandomHeightJigsawFeature.class, "Generated structure at " + blockPos);
            }

            return generator;
        }, PostPlacementProcessor.NONE);
    }
}
