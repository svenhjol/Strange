package svenhjol.strange.feature.stone_circles;

import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.common.CommonFeature;
import svenhjol.charm.charmony.common.CommonLoader;
import svenhjol.strange.feature.stone_circles.common.Providers;
import svenhjol.strange.feature.stone_circles.common.Registers;

@Feature
public final class StoneCircles extends CommonFeature {
    public final Registers registers;
    public final Providers providers;

    public StoneCircles(CommonLoader loader) {
        super(loader);

        registers = new Registers(this);
        providers = new Providers(this);
    }
}
