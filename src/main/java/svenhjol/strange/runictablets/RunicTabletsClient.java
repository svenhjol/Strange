package svenhjol.strange.runictablets;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

public class RunicTabletsClient extends CharmClientModule {
    public RunicTabletsClient(CharmModule module) {
        super(module);
    }

    @Override
    public void init() {
        ScreenRegistry.register(RunicTablets.SCREEN_HANDLER, RunicAltarScreen::new);
    }
}
