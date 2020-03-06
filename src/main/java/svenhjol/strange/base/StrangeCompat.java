package svenhjol.strange.base;

import svenhjol.meson.MesonInstance;
import svenhjol.meson.helper.ForgeHelper;
import svenhjol.strange.Strange;
import svenhjol.strange.base.compat.QuarkCompat;

public class StrangeCompat
{
    public static void init(MesonInstance instance)
    {
        try {
            if (ForgeHelper.isModLoaded("quark")) {
                Strange.quarkCompat = QuarkCompat.class.newInstance();
                instance.log.debug("Loaded Quark compatibility");
            }
        } catch (Exception e) {
            instance.log.error("Error loading Quark compatibility: " + e.getMessage());
        }
    }
}
