package svenhjol.strange.feature.runestones;

import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.common.CommonFeature;
import svenhjol.charm.charmony.common.CommonLoader;
import svenhjol.strange.feature.runestones.common.Handlers;
import svenhjol.strange.feature.runestones.common.Networking;
import svenhjol.strange.feature.runestones.common.Providers;
import svenhjol.strange.feature.runestones.common.Registers;

@Feature
public final class Runestones extends CommonFeature {
    public final Registers registers;
    public final Handlers handlers;
    public final Networking networking;
    public final Providers providers;

    public Runestones(CommonLoader loader) {
        super(loader);

        registers = new Registers(this);
        handlers = new Handlers(this);
        networking = new Networking(this);
        providers = new Providers(this);
    }
}
