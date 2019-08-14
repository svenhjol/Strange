package svenhjol.strange.totems.feature;

import net.minecraftforge.common.ForgeConfigSpec;
import svenhjol.meson.Feature;
import svenhjol.strange.totems.item.TotemOfReturningItem;

public class TotemOfReturning extends Feature
{
    public static ForgeConfigSpec.BooleanValue addToLoot;
    public static TotemOfReturningItem item;

    @Override
    public void configure()
    {
        super.configure();
    }

    @Override
    public void init()
    {
        super.init();
        item = new TotemOfReturningItem();
    }

    @Override
    public boolean hasSubscriptions()
    {
        return true;
    }
}
