package svenhjol.strange.world.gen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.base.structure.JigsawVariableHeightFeature;

public class CaveRuinFeature extends JigsawVariableHeightFeature {
    public CaveRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        // TODO: variation should be 12 when worldgen is restored
        super(codec, 24, 4, false);
    }
}
