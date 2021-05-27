package svenhjol.strange.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.structure.JigsawVariableHeightFeature;

public class NetherRuinFeature extends JigsawVariableHeightFeature {
    public NetherRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 16, 2, false);
    }
}
