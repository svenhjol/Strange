package svenhjol.strange;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import svenhjol.meson.iface.IMesonSidedProxy;
import svenhjol.strange.base.ClientProxy;
import svenhjol.strange.base.CommonProxy;
import svenhjol.strange.base.StrangeLoader;

@Mod(Strange.MOD_ID)
public class Strange
{
    public static final String MOD_ID = "strange";
    public static StrangeLoader loader;
    public static IMesonSidedProxy proxy = DistExecutor.runForDist(
        () -> ClientProxy::new, () -> CommonProxy::new);

    public Strange()
    {
        loader = new StrangeLoader();
        proxy.init();
    }
}
