package svenhjol.strange;

import net.minecraft.resources.ResourceLocation;
import svenhjol.charm_core.annotation.Feature;
import svenhjol.charm_core.iface.*;

public class Strange {
    public static final String MOD_ID = "strange";
    public static final String PREFIX = "svenhjol." + MOD_ID;
    public static final String FEATURE_PREFIX = PREFIX + ".feature";
    public static ILog LOG;
    public static ILoader LOADER;
    public static INetwork NETWORK;
    public static IRegistry REGISTRY;

    public Strange(IInitializer init) {
        LOG = init.getLog();
        LOADER = init.getLoader();
        NETWORK = init.getNetwork();
        REGISTRY = init.getRegistry();

        LOADER.init(FEATURE_PREFIX, Feature.class);
    }

    public void run() {
        LOADER.run();
    }

    public static ResourceLocation makeId(String id) {
        return !id.contains(":") ? new ResourceLocation(MOD_ID, id) : new ResourceLocation(id);
    }
}
