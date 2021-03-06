package svenhjol.strange.ruins;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class CaveRuinFeature extends JigsawFeature {
    public CaveRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 24, false, false);
    }
}
