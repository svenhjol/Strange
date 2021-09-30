package svenhjol.strange.init;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.helper.RegistryHelper;
import svenhjol.strange.Strange;

public class StrangeParticles {
    public static SimpleParticleType APPLY_PARTICLE;
    public static SimpleParticleType AXIS_PARTICLE;
    public static SimpleParticleType ORE_GLOW_PARTICLE;

    public static void init() {
        APPLY_PARTICLE = RegistryHelper.defaultParticleType(new ResourceLocation(Strange.MOD_ID, "apply"));
        AXIS_PARTICLE = RegistryHelper.defaultParticleType(new ResourceLocation(Strange.MOD_ID, "axis"));
        ORE_GLOW_PARTICLE = RegistryHelper.defaultParticleType(new ResourceLocation(Strange.MOD_ID, "ore_glow"));
    }
}
