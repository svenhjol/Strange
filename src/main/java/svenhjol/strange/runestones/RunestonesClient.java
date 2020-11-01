package svenhjol.strange.runestones;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import svenhjol.charm.base.CharmModule;
import svenhjol.strange.module.Runestones;

public class RunestonesClient {
    public RunestonesClient(CharmModule module) {
        // listen for player runestone discoveries being sent from the server
        ClientSidePacketRegistry.INSTANCE.register(Runestones.MSG_CLIENT_SYNC_DISCOVERIES, (context, data) -> {
            int[] discoveries = data.readIntArray();
            context.getTaskQueue().execute(() -> {
                RunestoneHelper.populateDiscoveredRunes(context.getPlayer(), discoveries);
            });
        });
    }
}
