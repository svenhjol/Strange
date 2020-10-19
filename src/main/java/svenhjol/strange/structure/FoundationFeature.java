package svenhjol.strange.structure;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;

public class FoundationFeature extends JigsawFeature {
    public FoundationFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 4, false, false);
    }
}
