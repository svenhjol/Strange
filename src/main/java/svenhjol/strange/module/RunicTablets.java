package svenhjol.strange.module;

import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.item.TabletItem;

@Module(mod = Strange.MOD_ID)
public class RunicTablets extends CharmModule {
    public static TabletItem CLAY_TABLET;
    public static TabletItem RUNIC_TABLET;

    @Override
    public void register() {
        CLAY_TABLET = new TabletItem(this, "clay_tablet");
        RUNIC_TABLET = new TabletItem(this, "runic_tablet");


    }
}
