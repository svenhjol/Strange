package svenhjol.strange.module.end_shrines;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import svenhjol.strange.module.structures.RandomHeightJigsawFeature;

public class EndShrineFeature extends RandomHeightJigsawFeature {
    public EndShrineFeature(Codec<JigsawConfiguration> codec, ResourceLocation starts, int size, int startY, int variation) {
        super(codec, starts, size, startY, variation, false, EndShrineFeature::checkLocation);
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        return worldgenRandom.nextInt(20) < 1;
    }
}
