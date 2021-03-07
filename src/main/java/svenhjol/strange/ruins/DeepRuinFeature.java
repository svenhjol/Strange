package svenhjol.strange.ruins;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class DeepRuinFeature extends JigsawFeature {
    public DeepRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, -61, false, false);
    }
}
