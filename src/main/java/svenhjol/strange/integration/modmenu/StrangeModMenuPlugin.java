package svenhjol.strange.integration.modmenu;

import svenhjol.charm.integration.modmenu.BaseModMenuPlugin;
import svenhjol.charm.loader.CharmModule;
import svenhjol.strange.Strange;

import java.util.List;

public class StrangeModMenuPlugin extends BaseModMenuPlugin<CharmModule> {
    @Override
    public String getModId() {
        return Strange.MOD_ID;
    }

    @Override
    public List<CharmModule> getModules() {
        return Strange.LOADER.getModules();
    }
}
