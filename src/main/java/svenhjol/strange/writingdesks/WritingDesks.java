package svenhjol.strange.writingdesks;

import net.minecraft.util.Identifier;
import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.handler.ModuleHandler;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.scrollkeepers.Scrollkeepers;

@Module(mod = Strange.MOD_ID, client = WritingDesksClient.class, description = "Writing desks are the job site for scrollkeepers.", alwaysEnabled = true)
public class WritingDesks extends CharmModule {
    public static Identifier BLOCK_ID = new Identifier(Strange.MOD_ID, "writing_desk");
    public static WritingDeskBlock WRITING_DESK;

    @Override
    public void register() {
        WRITING_DESK = new WritingDeskBlock(this);
        Scrollkeepers.registerAfterWritingDesk();
    }

    @Override
    public boolean depends() {
        return ModuleHandler.enabled("strange:scrolls");
    }
}
