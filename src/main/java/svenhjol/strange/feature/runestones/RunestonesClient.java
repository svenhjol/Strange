package svenhjol.strange.feature.runestones;

import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.client.ClientFeature;
import svenhjol.charm.charmony.client.ClientLoader;
import svenhjol.charm.charmony.feature.LinkedFeature;
import svenhjol.strange.feature.runestones.client.Handlers;
import svenhjol.strange.feature.runestones.client.Registers;

@Feature
public final class RunestonesClient extends ClientFeature implements LinkedFeature<Runestones> {
    public final Registers registers;
    public final Handlers handlers;

    public RunestonesClient(ClientLoader loader) {
        super(loader);

        registers = new Registers(this);
        handlers = new Handlers(this);
    }

    @Override
    public Class<Runestones> typeForLinked() {
        return Runestones.class;
    }
}
