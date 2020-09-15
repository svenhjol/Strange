package svenhjol.strange.structure;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class RuinFeature extends JigsawFeature {
    public RuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 32, false, false);
    }
}