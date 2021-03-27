package svenhjol.strange.ruins;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class EndRuinFeature extends JigsawFeature {
    public EndRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 100, false, false);
    }
}
