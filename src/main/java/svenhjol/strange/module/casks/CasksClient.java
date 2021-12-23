package svenhjol.strange.module.casks;

import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.casks.network.ClientReceiveAddToCask;

@ClientModule(module = Casks.class)
public class CasksClient extends CharmModule {
    public static ClientReceiveAddToCask CLIENT_RECEIVE_ADD_TO_CASK;

    @Override
    public void runWhenEnabled() {
        CLIENT_RECEIVE_ADD_TO_CASK = new ClientReceiveAddToCask();
    }
}
