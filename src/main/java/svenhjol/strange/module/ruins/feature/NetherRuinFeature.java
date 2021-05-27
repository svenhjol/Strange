package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import svenhjol.charm.world.CharmJigsawStructureFeature;

public class NetherRuinFeature extends CharmJigsawStructureFeature {
    public NetherRuinFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, 16, 2, false);
    }
}
