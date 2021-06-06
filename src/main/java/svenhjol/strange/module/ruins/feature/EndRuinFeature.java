package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import svenhjol.charm.world.CharmJigsawStructureFeature;

public class EndRuinFeature extends CharmJigsawStructureFeature {
    public EndRuinFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 100, 20, false);
    }
}
