package svenhjol.strange.module.stone_circles;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class StoneCircleConfiguration implements FeatureConfiguration {
    public static final Codec<StoneCircleConfiguration> CODEC;
    public final StoneCircleFeature.Type stoneCircleType;

    public StoneCircleConfiguration(StoneCircleFeature.Type stoneCircleType) {
        this.stoneCircleType = stoneCircleType;
    }

    static {
        CODEC = StoneCircleFeature.Type.CODEC.fieldOf("stone_circle_type").xmap(StoneCircleConfiguration::new, config -> config.stoneCircleType).codec();
    }
}
