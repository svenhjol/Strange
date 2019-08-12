package svenhjol.strange.totems;

import svenhjol.meson.Module;
import svenhjol.strange.totems.feature.TotemOfAttracting;
import svenhjol.strange.totems.feature.TotemOfReturning;
import svenhjol.strange.totems.feature.TotemOfShielding;

public class StrangeTotems extends Module
{
    public StrangeTotems()
    {
        features.add(new TotemOfAttracting());
        features.add(new TotemOfReturning());
        features.add(new TotemOfShielding());
    }
}
