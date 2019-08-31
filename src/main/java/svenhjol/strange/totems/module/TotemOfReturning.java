package svenhjol.strange.totems.module;

import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;
import svenhjol.strange.Strange;
import svenhjol.strange.base.StrangeCategories;
import svenhjol.strange.totems.item.TotemOfReturningItem;

@Module(mod = Strange.MOD_ID, category = StrangeCategories.TOTEMS, hasSubscriptions = true)
public class TotemOfReturning extends MesonModule
{
    public static TotemOfReturningItem item;

    @Override
    public void init()
    {
        item = new TotemOfReturningItem(this);
    }
}
