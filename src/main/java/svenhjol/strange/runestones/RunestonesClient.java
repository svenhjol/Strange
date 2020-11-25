package svenhjol.strange.runestones;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import svenhjol.charm.base.CharmClientModule;
import svenhjol.charm.base.CharmModule;

public class RunestonesClient extends CharmClientModule {
    public RunestonesClient(CharmModule module) {
        super(module);
    }

    @Override
    public void register() {
        // listen for player runestone discoveries being sent from the server
        ClientSidePacketRegistry.INSTANCE.register(Runestones.MSG_CLIENT_SYNC_LEARNED, (context, data) -> {
            int[] discoveries = data.readIntArray();
            context.getTaskQueue().execute(() -> {
                RunestoneHelper.populateLearnedRunes(context.getPlayer(), discoveries);
            });
        });
    }
}
