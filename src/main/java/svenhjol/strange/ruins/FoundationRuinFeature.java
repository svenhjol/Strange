package svenhjol.strange.ruins;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class FoundationRuinFeature extends JigsawFeature {
    public FoundationRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 3, false, false);
    }
}
