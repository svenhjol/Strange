package svenhjol.strange.totems.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Config;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.item.TotemOfHoldingItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfHolding extends MesonModule
{
    public static TotemOfHoldingItem item;

    @Config(name = "Durability", description = "Durability of the Totem.")
    public static int durability = 120;

    @Override
    public void init()
    {
        item = new TotemOfHoldingItem(this);
    }
}
