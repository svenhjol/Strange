package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.world.CharmJigsawStructureFeature;

public class DeepRuinFeature extends CharmJigsawStructureFeature {
    public DeepRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 3, 0, true);
    }
}
