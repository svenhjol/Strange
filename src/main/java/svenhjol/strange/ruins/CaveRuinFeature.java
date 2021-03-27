package svenhjol.strange.ruins;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.structure.JigsawVariableHeightFeature;

public class CaveRuinFeature extends JigsawVariableHeightFeature {
    public CaveRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 24, 12, false);
    }
}
