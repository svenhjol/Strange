package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import svenhjol.charm.world.CharmJigsawStructureFeature;

public class DeepRuinFeature extends CharmJigsawStructureFeature {
    public DeepRuinFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 3, 0, true);
    }
}
