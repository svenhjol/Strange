package svenhjol.strange;

import net.minecraftforge.fml.common.Mod;
import svenhjol.meson.Feature;
import svenhjol.strange.base.StrangeLoader;
import svenhjol.strange.totems.StrangeTotems;

@Mod(Strange.MOD_ID)
public class Strange
{
    public static final String MOD_ID = "strange";

    public Strange()
    {
        StrangeLoader.INSTANCE.registerLoader(Strange.MOD_ID).setup(
            new StrangeTotems()
        );
    }

    public static boolean hasFeature(Class<? extends Feature> feature)
    {
        return StrangeLoader.INSTANCE.enabledFeatures.containsKey(feature);
    }
}
