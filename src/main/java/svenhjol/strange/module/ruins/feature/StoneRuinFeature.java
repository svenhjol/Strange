package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import svenhjol.strange.module.structures.RandomHeightJigsawFeature;

public class StoneRuinFeature extends RandomHeightJigsawFeature {
    public StoneRuinFeature(Codec<JigsawConfiguration> codec, ResourceLocation starts, int size, int startY, int variation) {
        super(codec, starts, size, startY, variation, false, StoneRuinFeature::checkLocation);
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        return worldgenRandom.nextInt(5) >= 2;
    }
}
