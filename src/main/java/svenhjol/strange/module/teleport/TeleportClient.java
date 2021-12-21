package svenhjol.strange.module.teleport;

import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.teleport.iface.ITeleportType;
import svenhjol.strange.module.teleport.runic.RunicTeleportClient;

import java.util.ArrayList;
import java.util.List;

@ClientModule(module = Teleport.class)
public class TeleportClient extends CharmModule {
    public static final List<ITeleportType> TYPES = new ArrayList<>();

    @Override
    public void register() {
        TYPES.add(new RunicTeleportClient());
        TYPES.forEach(ITeleportType::register);
    }

    @Override
    public void runWhenEnabled() {
        TYPES.forEach(ITeleportType::runWhenEnabled);
    }
}
