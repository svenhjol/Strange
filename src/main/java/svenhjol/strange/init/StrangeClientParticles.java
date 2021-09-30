package svenhjol.strange.init;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import svenhjol.strange.client.StrangeParticle;

public class StrangeClientParticles {
    public static void init() {
        ParticleFactoryRegistry.getInstance().register(StrangeParticles.APPLY_PARTICLE, StrangeParticle.ApplyFactory::new);
        ParticleFactoryRegistry.getInstance().register(StrangeParticles.AXIS_PARTICLE, StrangeParticle.AxisFactory::new);
        ParticleFactoryRegistry.getInstance().register(StrangeParticles.ORE_GLOW_PARTICLE, StrangeParticle.OreGlowFactory::new);
    }
}
