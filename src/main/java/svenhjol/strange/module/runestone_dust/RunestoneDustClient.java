package svenhjol.strange.module.runestone_dust;

import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.runestones.Runestones;

@ClientModule(module = RunestoneDust.class)
public class RunestoneDustClient extends CharmModule {
    @Override
    public void register() {
        EntityRendererRegistry.register(RunestoneDust.RUNESTONE_DUST_ENTITY, RunestoneDustEntityRenderer::new);
    }
}
