package svenhjol.strange.module;

import svenhjol.charm.base.CharmModule;
import svenhjol.charm.base.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.iface.ILegendaryItem;
import svenhjol.strange.legendaryitems.EldritchBow;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID)
public class LegendaryItems extends CharmModule {
    public static Map<Integer, ILegendaryItem> LEGENDARY_ITEMS = new HashMap<>();

    @Override
    public void register() {
        LEGENDARY_ITEMS.put(1, new EldritchBow());
    }


}
