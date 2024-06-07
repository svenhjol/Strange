package svenhjol.strange;

import svenhjol.charm.charmony.client.ClientFeature;
import svenhjol.strange.feature.core.CoreClient;
import svenhjol.strange.feature.runestones.RunestonesClient;
import svenhjol.strange.feature.travel_journals.TravelJournalsClient;

import java.util.List;

public final class StrangeClient {
    public static List<Class<? extends ClientFeature>> features() {
        return List.of(
            CoreClient.class,
            RunestonesClient.class,
            TravelJournalsClient.class
        );
    }
}
