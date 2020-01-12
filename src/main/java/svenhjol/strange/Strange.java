package svenhjol.strange;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import svenhjol.meson.MesonModule;
import svenhjol.strange.base.StrangeLoader;

@Mod(Strange.MOD_ID)
public class Strange
{
    public static final String MOD_ID = "strange";

    public Strange()
    {
        new StrangeLoader();
    }

    public static boolean hasModule(Class<? extends MesonModule> module)
    {
        return StrangeLoader.hasModule(new ResourceLocation(Strange.MOD_ID, module.getSimpleName().toLowerCase()));
    }

    public static boolean hasModule(String module)
    {
        return StrangeLoader.hasModule(new ResourceLocation(Strange.MOD_ID, module));
    }
}
