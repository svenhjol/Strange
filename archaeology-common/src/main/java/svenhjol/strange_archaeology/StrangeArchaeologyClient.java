package svenhjol.strange_archaeology;

import svenhjol.charm_core.annotation.ClientFeature;
import svenhjol.charm_core.iface.IClientInitializer;
import svenhjol.charm_core.iface.IClientRegistry;
import svenhjol.charm_core.iface.ILoader;

public class StrangeArchaeologyClient {
    public static final String MOD_ID = "strange_archaeology";
    public static final String PREFIX = "svenhjol." + MOD_ID;
    public static final String FEATURE_PREFIX = PREFIX + ".feature";
    public static ILoader LOADER;
    public static IClientRegistry REGISTRY;

    public StrangeArchaeologyClient(IClientInitializer init) {
        LOADER = init.getLoader();
        REGISTRY = init.getRegistry();

        LOADER.init(FEATURE_PREFIX, ClientFeature.class);
    }

    public void run() {
        LOADER.run();
    }
}
