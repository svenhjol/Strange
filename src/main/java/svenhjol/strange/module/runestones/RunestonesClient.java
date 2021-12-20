package svenhjol.strange.module.runestones;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;

@ClientModule(module = Runestones.class)
public class RunestonesClient extends CharmModule {

    @Override
    public void register() {
        ScreenRegistry.register(Runestones.MENU, RunestoneScreen::new);
        EntityRendererRegistry.register(Runestones.RUNESTONE_DUST_ENTITY, RunestoneDustEntityRenderer::new);
    }
}
