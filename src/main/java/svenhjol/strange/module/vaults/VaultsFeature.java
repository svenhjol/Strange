package svenhjol.strange.module.vaults;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import svenhjol.strange.module.structures.RandomHeightJigsawFeature;

public class VaultsFeature extends RandomHeightJigsawFeature {
    public VaultsFeature(Codec<JigsawConfiguration> codec, ResourceLocation starts, int size, int startY, int variation) {
        super(codec, starts, size, startY, variation, false, VaultsFeature::checkLocation);
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));

        var x = context.chunkPos().x;
        var z = context.chunkPos().z;

        if (x < 100 && z < 100) {
            return false;
        }

        worldgenRandom.setLargeFeatureSeed(context.seed(), x, z);
        return worldgenRandom.nextInt(15) < 1;
    }
}
