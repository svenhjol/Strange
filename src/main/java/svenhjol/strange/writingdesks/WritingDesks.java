package svenhjol.strange.writingdesks;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.handler.RegistryHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.module.Scrollkeepers;

@Module(mod = Strange.MOD_ID, client = WritingDesksClient.class, description = "Writing desks are the job site for scrollkeepers and allow creation of runic tablets.", alwaysEnabled = true)
public class WritingDesks extends CharmModule {
    public static Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "writing_desk");
    public static WritingDeskBlock WRITING_DESK;
    public static ScreenHandlerType<WritingDeskScreenHandler> SCREEN_HANDLER;

    @Override
    public void register() {
        WRITING_DESK = new WritingDeskBlock(this);
        SCREEN_HANDLER = RegistryHandler.screenHandler(BLOCK_ID, WritingDeskScreenHandler::new);

        Scrollkeepers.registerAfterWritingDesk();

        enabled = ModuleHandler.enabled("strange:scrolls");
    }

}
