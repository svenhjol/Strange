package svenhjol.strange.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import svenhjol.strange.client.IllagerAltParticle;

@Environment(EnvType.CLIENT)
public class StrangeClientParticles {
    public static void init() {
        ParticleFactoryRegistry.getInstance().register(StrangeParticles.ILLAGERALT, IllagerAltParticle.Provider::new);
    }
}
