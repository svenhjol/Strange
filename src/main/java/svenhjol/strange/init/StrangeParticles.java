package svenhjol.strange.init;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import svenhjol.charm.registry.CommonRegistry;
import svenhjol.strange.Strange;

public class StrangeParticles {
    public static SimpleParticleType ILLAGERALT;

    public static void init() {
        ILLAGERALT = CommonRegistry.defaultParticleType(new ResourceLocation(Strange.MOD_ID, "illageralt"));
    }
}
