package svenhjol.strange.module.potion_of_spelunking;

import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.potion_of_spelunking.network.ClientReceiveShowParticles;

@ClientModule(module = PotionOfSpelunking.class)
public class PotionOfSpelunkingClient extends CharmModule {
    public static ClientReceiveShowParticles CLIENT_RECEIVE_SHOW_PARTICLES;

    @Override
    public void register() {
        CLIENT_RECEIVE_SHOW_PARTICLES = new ClientReceiveShowParticles();
    }
}
