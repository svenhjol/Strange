package svenhjol.strange.ruins;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.structure.JigsawVariableHeightFeature;

public class EndRuinFeature extends JigsawVariableHeightFeature {
    public EndRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 100, 20, false);
    }
}
