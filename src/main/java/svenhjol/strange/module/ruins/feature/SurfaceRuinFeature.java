package svenhjol.strange.module.ruins.feature;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.JigsawFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;

public class SurfaceRuinFeature extends JigsawFeature {
    public SurfaceRuinFeature(Codec<JigsawConfiguration> codec) {
        super(codec, 64, true, true);
    }
}
