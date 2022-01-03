package svenhjol.strange.module.runic_tomes;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.world.item.ItemStack;
import svenhjol.charm.annotation.ClientModule;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.module.runic_tomes.network.ClientReceiveSetTome;

@ClientModule(module = RunicTomes.class)
public class RunicTomesClient extends CharmModule {
    public static ItemStack tomeHolder = null;

    public static ClientReceiveSetTome CLIENT_RECEIVE_SET_TOME;

    @Override
    public void register() {
        ScreenRegistry.register(RunicTomes.RUNIC_LECTERN_MENU, RunicLecternScreen::new);
        BlockEntityRendererRegistry.register(RunicTomes.RUNIC_LECTERN_BLOCK_ENTITY, RunicLecternRenderer::new);
    }

    @Override
    public void runWhenEnabled() {
        CLIENT_RECEIVE_SET_TOME = new ClientReceiveSetTome();
    }
}
