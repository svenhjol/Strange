package svenhjol.strange.feature.travel_journals;

import svenhjol.charm.charmony.annotation.Feature;
import svenhjol.charm.charmony.client.ClientFeature;
import svenhjol.charm.charmony.client.ClientLoader;
import svenhjol.charm.charmony.feature.LinkedFeature;
import svenhjol.strange.feature.travel_journals.client.Handlers;
import svenhjol.strange.feature.travel_journals.client.Registers;

@Feature
public final class TravelJournalsClient extends ClientFeature implements LinkedFeature<TravelJournals> {
    public final Registers registers;
    public final Handlers handlers;

    public TravelJournalsClient(ClientLoader loader) {
        super(loader);

        registers = new Registers(this);
        handlers = new Handlers(this);
    }

    @Override
    public Class<TravelJournals> typeForLinked() {
        return TravelJournals.class;
    }
}
