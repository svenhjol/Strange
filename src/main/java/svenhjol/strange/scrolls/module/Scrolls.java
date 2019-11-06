package svenhjol.strange.scrolls.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.scrolls.item.ScrollItem;

import java.util.HashMap;
import java.util.Map;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.SCROLLS)
public class Scrolls extends MesonModule
{
    public static int MAX_TIERS = 5;

    public static Map<Integer, ScrollItem> tiers = new HashMap<>();

    @Override
    public void init()
    {
        for (int i = 1; i <= MAX_TIERS; i++) {
            tiers.put(i, new ScrollItem(this, i));
        }
    }
}
