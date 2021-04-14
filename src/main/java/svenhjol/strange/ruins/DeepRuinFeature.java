package svenhjol.strange.ruins;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class DeepRuinFeature extends JigsawFeature {
    public DeepRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        // TODO: structureStartY should be -61 when worldgen is restored
        super(codec, 3, false, false);
    }
}
