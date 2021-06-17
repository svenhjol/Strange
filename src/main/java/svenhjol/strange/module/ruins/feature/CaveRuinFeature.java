package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import svenhjol.charm.world.CharmJigsawStructureFeature;

public class CaveRuinFeature extends CharmJigsawStructureFeature {
    public CaveRuinFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 32, 8, false);
    }
}
