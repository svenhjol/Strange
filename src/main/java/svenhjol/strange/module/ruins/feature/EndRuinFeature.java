package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.world.CharmJigsawStructureFeature;

public class EndRuinFeature extends CharmJigsawStructureFeature {
    public EndRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 100, 20, false);
    }
}
