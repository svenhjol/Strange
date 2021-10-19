package svenhjol.strange.module.writing_desks;

import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

@ClientModule(module = WritingDesks.class)
public class WritingDesksClient extends CharmModule {
    @Override
    public void runWhenEnabled() {
        ScreenRegistry.register(WritingDesks.MENU, WritingDeskScreen::new);
    }
}
