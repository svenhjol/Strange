package svenhjol.strange.ruins;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class UndergroundRuinFeature extends JigsawFeature {
    public UndergroundRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 24, false, false);
    }
}
