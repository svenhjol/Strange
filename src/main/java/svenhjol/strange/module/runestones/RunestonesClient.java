package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.runestones.network.ClientReceiveRunestoneClues;
import svenhjol.strange.module.runestones.network.ClientReceiveRunestoneItems;

@ClientModule(module = Runestones.class)
public class RunestonesClient extends CharmModule {
    public static ClientReceiveRunestoneItems CLIENT_RECEIVE_RUNESTONE_ITEMS;
    public static ClientReceiveRunestoneClues CLIENT_RECEIVE_RUNESTONE_CLUES;

    @Override
    public void register() {
        ScreenRegistry.register(Runestones.MENU, RunestoneScreen::new);
    }

    @Override
    public void runWhenEnabled() {
        CLIENT_RECEIVE_RUNESTONE_ITEMS = new ClientReceiveRunestoneItems();
        CLIENT_RECEIVE_RUNESTONE_CLUES = new ClientReceiveRunestoneClues();
    }
}
