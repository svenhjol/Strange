package svenhjol.strange.base;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import svenhjol.strange.Strange;

public class ClientProxy extends CommonProxy
{
    @Override
    public void init()
    {
        super.init();

        Strange.loader.bus.addListener(this::setupClient);
    }

    public void setupClient(FMLClientSetupEvent event)
    {
        Strange.loader.setupClient(event);
    }
}
