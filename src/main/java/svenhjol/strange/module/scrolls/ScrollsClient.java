package svenhjol.strange.module.scrolls;

import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.scrolls.network.ClientReceiveDestroyScroll;
import svenhjol.strange.module.scrolls.network.ClientReceiveOpenScroll;

@ClientModule(module = Scrolls.class)
public class ScrollsClient extends CharmModule {
    public static ClientReceiveDestroyScroll CLIENT_RECEIVE_DESTROY_SCROLL;
    public static ClientReceiveOpenScroll CLIENT_RECEIVE_OPEN_SCROLL;

    @Override
    public void runWhenEnabled() {
        CLIENT_RECEIVE_DESTROY_SCROLL = new ClientReceiveDestroyScroll();
        CLIENT_RECEIVE_OPEN_SCROLL = new ClientReceiveOpenScroll();
    }
}
