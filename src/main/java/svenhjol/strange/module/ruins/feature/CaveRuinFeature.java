package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.world.CharmJigsawStructureFeature;

public class CaveRuinFeature extends CharmJigsawStructureFeature {
    public CaveRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        // TODO: variation should be 12 when worldgen is restored
        super(codec, 24, 4, false);
    }
}
