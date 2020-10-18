package svenhjol.strange.module;

import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.block.AncientRubbleBlock;

@Module(mod = Strange.MOD_ID)
public class Excavation extends CharmModule {
    public static AncientRubbleBlock ANCIENT_RUBBLE;

    @Override
    public void register() {
        ANCIENT_RUBBLE = new AncientRubbleBlock(this);
    }
}
