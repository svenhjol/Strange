package svenhjol.strange.base;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.meson.iface.IMesonSidedProxy;
import svenhjol.strange.Strange;
import svenhjol.strange.totems.StrangeTotems;

public class CommonProxy implements IMesonSidedProxy
{
    @Override
    public void init()
    {
        Strange.loader.add(
            new StrangeTotems()
        );

        Strange.loader.bus.addListener(this::setup);
    }

    public void setup(FMLCommonSetupEvent event)
    {
        Strange.loader.setup(event);
    }
}
