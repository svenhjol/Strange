package svenhjol.strange;

import net.minecraftforge.fml.common.Mod;
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
}
