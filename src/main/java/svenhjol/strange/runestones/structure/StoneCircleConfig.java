package svenhjol.strange.runestones.structure;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class StoneCircleConfig implements IFeatureConfig
{
    public static final String PROBABILITY = "probability";
    public final float probability;

    public StoneCircleConfig(float probability)
    {
        this.probability = probability;
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops)
    {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(ops.createString(PROBABILITY), ops.createFloat(this.probability))));
    }

    public static <T> StoneCircleConfig deserialize(Dynamic<T> ops)
    {
        float probability = ops.get(PROBABILITY).asFloat(0.0F);
        return new StoneCircleConfig(probability);
    }
}
