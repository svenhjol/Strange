package svenhjol.strange.module;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ClientRegistryHandler;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.block.WritingDeskBlock;
import svenhjol.strange.gui.WritingDeskScreen;
import svenhjol.strange.screenhandler.WritingDeskScreenHandler;

@Module(mod = Strange.MOD_ID, description = "Writing desks allow creation of runic tablets.", alwaysEnabled = true)
public class WritingDesks extends CharmModule {
    public static Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "writing_desk");
    public static WritingDeskBlock WRITING_DESK;
    public static ScreenHandlerType<WritingDeskScreenHandler> SCREEN_HANDLER;

    @Override
    public void register() {
        WRITING_DESK = new WritingDeskBlock(this);
        SCREEN_HANDLER = RegistryHandler.screenHandler(BLOCK_ID, WritingDeskScreenHandler::new);

        Scrollkeepers.registerAfterWritingDesk();

        enabled = ModuleHandler.enabled("strange:scrolls")
            && ModuleHandler.enabled("strange:runic_tablets");
    }

    @Override
    public void clientRegister() {
        BlockRenderLayerMap.INSTANCE.putBlock(WRITING_DESK, RenderLayer.getCutout());
    }

    @Override
    public void clientInit() {
        ClientRegistryHandler.screenHandler(SCREEN_HANDLER, WritingDeskScreen::new);
    }
}
