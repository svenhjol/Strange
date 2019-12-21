package svenhjol.strange;

import net.minecraftforge.fml.common.Mod;
import svenhjol.meson.MesonModule;
import svenhjol.strange.base.StrangeLoader;

@Mod(Strange.MOD_ID)
public class Strange
{
    public static final String MOD_ID = "strange";
    public static StrangeLoader loader;

    public Strange()
    {
        loader = new StrangeLoader();
    }

    public static boolean hasModule(Class<? extends MesonModule> module)
    {
        return loader != null && loader.hasModule(module);
    }

    public static boolean hasModule(String module)
    {
        return loader != null && loader.hasModule(module);
    }
}
