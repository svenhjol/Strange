package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import svenhjol.charm.world.CharmJigsawStructureFeature;

public class NetherRuinFeature extends CharmJigsawStructureFeature {
    public NetherRuinFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 16, 2, false);
    }
}
