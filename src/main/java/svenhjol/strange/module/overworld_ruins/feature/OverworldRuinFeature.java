package svenhjol.strange.module.overworld_ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import svenhjol.strange.structure.RandomHeightJigsawFeature;

public class OverworldRuinFeature extends RandomHeightJigsawFeature {
    public OverworldRuinFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 8, 16, false, OverworldRuinFeature::checkLocation);
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
        worldgenRandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        return worldgenRandom.nextInt(5) >= 2;
    }
}
